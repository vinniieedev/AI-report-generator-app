package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.model.AIRequest;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.ReportChart;
import com.paysecure.ai_report_tool_backend.model.UploadedFile;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.AIRequestRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportChartRepository;
import com.paysecure.ai_report_tool_backend.repository.UploadedFileRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String defaultModel;

    private final AIRequestRepository aiRequestRepository;
    private final ReportChartRepository chartRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public OpenAIService(
            AIRequestRepository aiRequestRepository,
            ReportChartRepository chartRepository,
            UploadedFileRepository uploadedFileRepository
    ) {
        this.aiRequestRepository = aiRequestRepository;
        this.chartRepository = chartRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String generateReport(
            Report report,
            User user,
            String systemPrompt,
            String userPrompt,
            Map<String, String> inputs
    ) throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }

        // Get uploaded files for this report
        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByReport(report);

        // Build the full prompt
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(userPrompt).append("\n\n");
        fullPrompt.append("Report Configuration:\n");
        fullPrompt.append("- Industry: ").append(report.getIndustry()).append("\n");
        fullPrompt.append("- Report Type: ").append(report.getReportType()).append("\n");
        fullPrompt.append("- Audience: ").append(report.getAudience()).append("\n");
        fullPrompt.append("- Purpose: ").append(report.getPurpose()).append("\n");
        fullPrompt.append("- Tone: ").append(report.getTone()).append("\n");
        fullPrompt.append("- Depth: ").append(report.getDepth()).append("\n\n");

        if (inputs != null && !inputs.isEmpty()) {
            fullPrompt.append("User Input Data:\n");
            inputs.forEach((key, value) -> 
                fullPrompt.append("- ").append(key).append(": ").append(value).append("\n")
            );
            fullPrompt.append("\n");
        }

        // Include uploaded file data
        if (!uploadedFiles.isEmpty()) {
            fullPrompt.append("UPLOADED DATA FILES:\n");
            fullPrompt.append("===================\n\n");
            
            for (UploadedFile file : uploadedFiles) {
                fullPrompt.append("File: ").append(file.getOriginalFilename()).append("\n");
                fullPrompt.append("Type: ").append(file.getContentType()).append("\n");
                
                if (file.getExtractedDataJson() != null) {
                    fullPrompt.append("Structured Data:\n");
                    fullPrompt.append(file.getExtractedDataJson()).append("\n\n");
                } else if (file.getExtractedText() != null) {
                    // Limit text to prevent token overflow
                    String text = file.getExtractedText();
                    if (text.length() > 5000) {
                        text = text.substring(0, 5000) + "...[truncated]";
                    }
                    fullPrompt.append("Content:\n").append(text).append("\n\n");
                }
            }
        }

        fullPrompt.append("\n\nIMPORTANT: Analyze the data provided and generate insights with data visualization recommendations.");
        fullPrompt.append("\nAt the end of your report, include a section called '## Data Visualizations' with JSON blocks for charts.");
        fullPrompt.append("\nUse this format for each chart:\n");
        fullPrompt.append("```chart\n{\"type\": \"pie|bar|line|doughnut\", \"title\": \"Chart Title\", \"data\": {\"labels\": [...], \"datasets\": [{\"data\": [...], \"backgroundColor\": [...]}]}}\n```\n");

        // Create request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", defaultModel);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 4000);

        JsonArray messages = new JsonArray();
        
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt != null ? systemPrompt : getDefaultSystemPrompt());
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", fullPrompt.toString());
        messages.add(userMessage);

        requestBody.add("messages", messages);

        // Make API call
        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        AIRequest aiRequest = new AIRequest();
        aiRequest.setReport(report);
        aiRequest.setUser(user);
        aiRequest.setModel(defaultModel);

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                aiRequest.setStatus("error");
                aiRequest.setErrorMessage("API Error: " + response.code() + " - " + responseBody);
                aiRequestRepository.save(aiRequest);
                throw new IOException("OpenAI API error: " + response.code());
            }

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract usage
            if (jsonResponse.has("usage")) {
                JsonObject usage = jsonResponse.getAsJsonObject("usage");
                aiRequest.setPromptTokens(usage.get("prompt_tokens").getAsInt());
                aiRequest.setCompletionTokens(usage.get("completion_tokens").getAsInt());
                aiRequest.setTotalTokens(usage.get("total_tokens").getAsInt());
                
                // Estimate cost (approximate rates)
                double cost = (aiRequest.getPromptTokens() * 0.00003) + (aiRequest.getCompletionTokens() * 0.00006);
                aiRequest.setCostUsd(BigDecimal.valueOf(cost));
            }

            // Extract content
            String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            aiRequest.setStatus("success");
            aiRequestRepository.save(aiRequest);

            // Extract and save charts from the response
            extractAndSaveCharts(content, report);

            return content;

        } catch (Exception e) {
            aiRequest.setStatus("error");
            aiRequest.setErrorMessage(e.getMessage());
            aiRequestRepository.save(aiRequest);
            throw e;
        }
    }

    private void extractAndSaveCharts(String content, Report report) {
        // Find all chart JSON blocks
        String chartPattern = "```chart\\s*\\n([\\s\\S]*?)\\n```";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(chartPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        int order = 0;
        while (matcher.find()) {
            String chartJson = matcher.group(1).trim();
            try {
                JsonObject chartData = gson.fromJson(chartJson, JsonObject.class);
                
                ReportChart chart = new ReportChart();
                chart.setReport(report);
                chart.setChartType(chartData.has("type") ? chartData.get("type").getAsString() : "bar");
                chart.setTitle(chartData.has("title") ? chartData.get("title").getAsString() : "Chart " + (order + 1));
                chart.setDataJson(chartData.has("data") ? chartData.get("data").toString() : chartJson);
                chart.setOptionsJson(chartData.has("options") ? chartData.get("options").toString() : null);
                chart.setSortOrder(order++);

                chartRepository.save(chart);
            } catch (Exception e) {
                // Skip invalid chart JSON
                System.err.println("Failed to parse chart JSON: " + e.getMessage());
            }
        }
    }

    private String getDefaultSystemPrompt() {
        return """
            You are an expert financial analyst and report writer. Your task is to generate professional, 
            accurate, and insightful financial reports based on the provided data and parameters.
            
            Guidelines:
            1. Structure the report clearly with sections and headings using Markdown
            2. Use appropriate financial terminology based on the audience
            3. Include calculations and analysis where relevant
            4. Provide actionable insights and recommendations
            5. Maintain the requested tone and depth throughout
            6. Use tables and bullet points for clarity where appropriate
            7. Include disclaimers where necessary for financial advice
            
            IMPORTANT - Data Visualization:
            When analyzing data, identify opportunities for visual representation and include chart specifications.
            For each recommended chart, use this exact format:
            
            ```chart
            {"type": "bar", "title": "Revenue by Quarter", "data": {"labels": ["Q1", "Q2", "Q3", "Q4"], "datasets": [{"label": "Revenue", "data": [100, 150, 200, 180], "backgroundColor": ["#5fcfee", "#9fb3f5", "#e9a9c4", "#fde2b8"]}]}}
            ```
            
            Chart types available: pie, bar, line, doughnut, scatter
            Use the brand colors: #5fcfee (cyan), #9fb3f5 (purple-blue), #e9a9c4 (pink), #fde2b8 (peach)
            
            Format the output in clean, readable Markdown format.
            """;
    }
}

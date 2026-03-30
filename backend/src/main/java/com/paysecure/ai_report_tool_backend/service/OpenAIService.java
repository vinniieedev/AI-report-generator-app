package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.model.AIRequest;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.ReportChart;
import com.paysecure.ai_report_tool_backend.model.UploadedFile;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.AIRequestRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportChartRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
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
    private final ReportRepository reportRepository;

    public OpenAIService(
            AIRequestRepository aiRequestRepository,
            ReportChartRepository chartRepository,
            UploadedFileRepository uploadedFileRepository,
            ReportRepository reportRepository
    ) {
        this.aiRequestRepository = aiRequestRepository;
        this.chartRepository = chartRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.reportRepository = reportRepository;
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
            String calculationPrompt,
            String outputFormatPrompt,
            Map<String, String> inputs
    ) throws IOException {

        if (!isConfigured()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }

        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByReport(report);

        StringBuilder fullPrompt = new StringBuilder();

        if (calculationPrompt != null && !calculationPrompt.isBlank()) {
            fullPrompt.append(calculationPrompt).append("\n\n");
        }

        fullPrompt.append("REPORT CONTEXT:\n");
        fullPrompt.append("- Industry: ").append(report.getIndustry()).append("\n");
        fullPrompt.append("- Report Type: ").append(report.getReportType()).append("\n");
        fullPrompt.append("- Audience: ").append(report.getAudience()).append("\n");
        fullPrompt.append("- Purpose: ").append(report.getPurpose()).append("\n");
        fullPrompt.append("- Tone: ").append(report.getTone()).append("\n");
        fullPrompt.append("- Depth: ").append(report.getDepth()).append("\n\n");

        if (inputs != null && !inputs.isEmpty()) {
            fullPrompt.append("DATA INPUTS (Authoritative Source):\n");
            fullPrompt.append(gson.toJson(inputs)).append("\n\n");
        }

        if (!uploadedFiles.isEmpty()) {
            fullPrompt.append("UPLOADED DATA FILES:\n\n");
            for (UploadedFile file : uploadedFiles) {
                fullPrompt.append("File: ").append(file.getOriginalFilename()).append("\n");

                if (file.getExtractedDataJson() != null) {
                    fullPrompt.append(file.getExtractedDataJson()).append("\n\n");
                } else if (file.getExtractedText() != null) {
                    String text = file.getExtractedText();
                    if (text.length() > 5000) {
                        text = text.substring(0, 5000) + "...[truncated]";
                    }
                    fullPrompt.append(text).append("\n\n");
                }
            }
        }

        if (outputFormatPrompt != null && !outputFormatPrompt.isBlank()) {
            fullPrompt.append("\n").append(outputFormatPrompt).append("\n");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", defaultModel);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 3000);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty(
                "content",
                (systemPrompt != null && !systemPrompt.isBlank())
                        ? systemPrompt
                        : getDefaultSystemPrompt()
        );
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", fullPrompt.toString());
        messages.add(userMessage);

        requestBody.add("messages", messages);

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
                aiRequest.setErrorMessage("API Error: " + response.code());
                aiRequestRepository.save(aiRequest);
                throw new IOException("OpenAI API error: " + response.code());
            }

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("usage")) {
                JsonObject usage = jsonResponse.getAsJsonObject("usage");
                aiRequest.setPromptTokens(usage.get("prompt_tokens").getAsInt());
                aiRequest.setCompletionTokens(usage.get("completion_tokens").getAsInt());
                aiRequest.setTotalTokens(usage.get("total_tokens").getAsInt());

                double cost =
                        (aiRequest.getPromptTokens() * 0.00003)
                                + (aiRequest.getCompletionTokens() * 0.00006);

                aiRequest.setCostUsd(BigDecimal.valueOf(cost));
            }

            String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            String finalMarkdown = parseAndPersistStructuredResponse(content, report);
            report.setContent(finalMarkdown);
            reportRepository.save(report);
            aiRequest.setStatus("success");
            aiRequestRepository.save(aiRequest);

            return finalMarkdown;

        } catch (Exception e) {
            aiRequest.setStatus("error");
            aiRequest.setErrorMessage(e.getMessage());
            aiRequestRepository.save(aiRequest);
            throw e;
        }
    }

    private String parseAndPersistStructuredResponse(String content, Report report) {

        try {

            // 🔥 STEP 1: Remove markdown code fences if present
            content = content.trim();

            if (content.startsWith("```")) {
                content = content
                        .replaceFirst("^```json", "")
                        .replaceFirst("^```", "")
                        .replaceAll("```$", "")
                        .trim();
            }

            // 🔥 STEP 2: Parse JSON
            JsonObject structured = gson.fromJson(content, JsonObject.class);

            // 🔥 STEP 3: Clear existing charts
            chartRepository.deleteByReport(report);

            // 🔥 STEP 4: Extract sections
            String summary = structured.has("summary")
                    ? structured.get("summary").getAsString()
                    : "";

            String calculations = structured.has("calculations")
                    ? structured.get("calculations").getAsString()
                    : "";

            String recommendations = structured.has("recommendations")
                    ? structured.get("recommendations").getAsString()
                    : "";

            String disclaimer = structured.has("disclaimer")
                    ? structured.get("disclaimer").getAsString()
                    : "";

            // 🔥 STEP 5: Save charts
            if (structured.has("charts")) {

                JsonArray charts = structured.getAsJsonArray("charts");

                int order = 0;

                for (int i = 0; i < charts.size(); i++) {

                    JsonObject chartData = charts.get(i).getAsJsonObject();

                    ReportChart chart = new ReportChart();
                    chart.setReport(report);
                    chart.setChartType(chartData.get("type").getAsString());
                    chart.setTitle(chartData.get("title").getAsString());
                    chart.setDataJson(chartData.get("data").toString());
                    chart.setOptionsJson(
                            chartData.has("options")
                                    ? chartData.get("options").toString()
                                    : null
                    );
                    chart.setSortOrder(order++);

                    chartRepository.save(chart);
                }
            }

            // 🔥 STEP 6: Return clean markdown
            return summary + "\n\n"
                    + calculations + "\n\n"
                    + recommendations + "\n\n"
                    + disclaimer;

        } catch (Exception e) {

            System.out.println("STRUCTURED PARSE FAILED");
            e.printStackTrace();

            return content;
        }
    }

    private String getDefaultSystemPrompt() {
        return """
                You are a senior financial analyst and structured report generator.
                
                Strict Rules:
                1. Never fabricate numbers.
                2. Only use provided data.
                3. If data is missing, clearly state assumptions.
                4. All totals must match calculated values.
                5. Show formulas before results when calculating.
                6. Maintain logical consistency throughout.
                7. Use structured Markdown with clear headings.
                
                Chart Rules:
                - Charts must follow the exact JSON schema provided.
                - Use only allowed chart types.
                - Ensure data arrays match label counts.
                - Do not include commentary inside chart JSON blocks.
                
                Output Discipline:
                - No conversational language.
                - No explanations about being an AI.
                - No deviation from required format.
            """;
    }
}

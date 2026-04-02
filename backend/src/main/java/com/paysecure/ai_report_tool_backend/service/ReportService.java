package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.CreateReportRequest;
import com.paysecure.ai_report_tool_backend.dto.ReportResponse;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.ReportChart;
import com.paysecure.ai_report_tool_backend.model.ReportInput;
import com.paysecure.ai_report_tool_backend.model.ReportPromptConfig;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.model.UploadedFile;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.enums.ReportStatus;
import com.paysecure.ai_report_tool_backend.model.enums.TransactionType;
import com.paysecure.ai_report_tool_backend.repository.ReportChartRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportPromptConfigRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import com.paysecure.ai_report_tool_backend.repository.UploadedFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportService {

    private static final int CREDITS_PER_REPORT = 1;

    private final ReportRepository reportRepository;
    private final ReportTemplateRepository templateRepository;
    private final ReportPromptConfigRepository promptConfigRepository;
    private final ReportChartRepository chartRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final CreditService creditService;
    private final OpenAIService openAIService;

    public ReportService(
            ReportRepository reportRepository,
            ReportTemplateRepository templateRepository,
            ReportPromptConfigRepository promptConfigRepository,
            ReportChartRepository chartRepository,
            UploadedFileRepository uploadedFileRepository,
            CreditService creditService,
            OpenAIService openAIService
    ) {
        this.reportRepository = reportRepository;
        this.templateRepository = templateRepository;
        this.promptConfigRepository = promptConfigRepository;
        this.chartRepository = chartRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.creditService = creditService;
        this.openAIService = openAIService;
    }

    /* -------------------------
       CREATE REPORT
    ------------------------- */
    @Transactional
    public ReportResponse create(CreateReportRequest req, User user) {

        Report report = new Report();
        report.setToolId(req.tool_id());
        report.setTitle(req.title());
        report.setIndustry(req.industry());
        report.setReportType(req.report_type());
        report.setAudience(req.audience());
        report.setPurpose(req.purpose());
        report.setTone(req.tone());
        report.setDepth(req.depth());
        report.setWizardData(req.wizard_data() != null ? req.wizard_data().toString() : "{}");
        report.setUser(user);
        report.setStatus(ReportStatus.DRAFT);
        report.setContent("");

        // Depending on what parameter should I try to search among already existing template

        ReportTemplate template = templateRepository.findByToolId(req.tool_id())
                .orElseThrow(() -> new ApiException("Template not found", HttpStatus.NOT_FOUND));

        report.setTemplate(template);
//        report.setReportTemplate();

        // Link to template if exists
        templateRepository.findByToolId(req.tool_id())
                .ifPresent(report::setTemplate);

        // Store inputs
        if (req.inputs() != null) {
            req.inputs().forEach((key, value) -> {
                ReportInput input = new ReportInput();
                input.setFieldKey(key);
                input.setValue(value);
                report.addInput(input);
            });
        }

        return toResponse(reportRepository.save(report));
    }

    /* -------------------------
       GENERATE REPORT
    ------------------------- */

    @Transactional
    public ReportResponse generate(UUID reportId, User user){

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (!creditService.hasEnoughCredits(user, CREDITS_PER_REPORT)) {
            throw new ApiException("Insufficient credits", HttpStatus.PAYMENT_REQUIRED);
        }

        report.setStatus(ReportStatus.PROCESSING);
        reportRepository.save(report);

        try {

            ReportTemplate template = report.getTemplate();

            if (template == null) {
                throw new ApiException("Template not found", HttpStatus.NOT_FOUND);
            }

            String systemPrompt = template.getSystemPrompt();
            String calculationPrompt = template.getCalculationPrompt();
            String outputFormatPrompt = template.getOutputFormatPrompt();

            Map<String, String> inputs = new HashMap<>();
            report.getInputs().forEach(input ->
                    inputs.put(input.getFieldKey(), input.getValue())
            );

            String aiResponse;

            if (openAIService.isConfigured()) {

                aiResponse = openAIService.generateReport(
                        report,
                        user,
                        systemPrompt,
                        calculationPrompt,
                        outputFormatPrompt,
                        inputs
                );

                report.setAiModel("gpt-4o");

            } else {

                aiResponse = generateMockReport(report, inputs);
                report.setAiModel("mock");
            }

            String startTag = "---CHARTS_JSON_START---";
            String endTag = "---CHARTS_JSON_END---";

            int start = aiResponse.indexOf(startTag);
            int end = aiResponse.indexOf(endTag);

            String chartsJson = null;
            String cleanContent = aiResponse;

            if (start != -1 && end != -1 && end > start) {

                chartsJson = aiResponse.substring(
                        start + startTag.length(),
                        end
                ).trim();

                cleanContent = cleanContent = sanitizeMarkdownTables(aiResponse.substring(0, start).trim());
            }

            report.setContent(cleanContent);
            reportRepository.save(report);

            if (chartsJson != null && !chartsJson.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(chartsJson);
                    com.fasterxml.jackson.databind.JsonNode charts = root.get("charts");

                    int order = 0;

                    if (charts != null && charts.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode chart : charts) {

                            ReportChart rc = new ReportChart();
                            rc.setReport(report);
                            rc.setChartType(chart.get("chartType").asText());
                            rc.setTitle(chart.get("title").asText());

                            rc.setDataJson(chart.get("data").toString());

                            rc.setOptionsJson(
                                    chart.has("options") ? chart.get("options").toString() : "{}"
                            );

                            rc.setSortOrder(order++);
                            chartRepository.save(rc);
                        }
                    }

                } catch (Exception e) {
                    log.error("Chart parsing failed", e);
                }
            }

            creditService.deductCredits(
                    user,
                    CREDITS_PER_REPORT,
                    TransactionType.REPORT_USAGE,
                    reportId.toString(),
                    "Generated report: " + report.getTitle()
            );

            report.setStatus(ReportStatus.GENERATED);
            report.setCreditsUsed(CREDITS_PER_REPORT);
            report.setCompletedAt(Instant.now());

        } catch (Exception e) {

            report.setStatus(ReportStatus.FAILED);
            report.setContent("Report generation failed: " + e.getMessage());
            reportRepository.save(report);

            throw new ApiException("Report generation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return toResponse(reportRepository.save(report));
    }

    /* -------------------------
       GET USER REPORTS
    ------------------------- */
    public List<ReportResponse> getUserReports(User user) {
        return reportRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* -------------------------
       GET SINGLE REPORT
    ------------------------- */
    public ReportResponse getReport(UUID reportId, User user) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        return toResponse(report);
    }

    /* -------------------------
       MOCK REPORT GENERATOR (when OpenAI not configured)
    ------------------------- */
    private String generateMockReport(Report report, Map<String, String> inputs) {
        StringBuilder content = new StringBuilder();
        content.append("# ").append(report.getTitle()).append("\n\n");
        content.append("## Executive Summary\n\n");
        content.append("This report provides a comprehensive analysis based on the provided data.\n\n");

        content.append("## Report Details\n\n");
        content.append("- **Industry:** ").append(report.getIndustry()).append("\n");
        content.append("- **Report Type:** ").append(report.getReportType()).append("\n");
        content.append("- **Audience:** ").append(report.getAudience()).append("\n");
        content.append("- **Purpose:** ").append(report.getPurpose()).append("\n");
        content.append("- **Tone:** ").append(report.getTone()).append("\n");
        content.append("- **Depth:** ").append(report.getDepth()).append("\n\n");

        if (!inputs.isEmpty()) {
            content.append("## Input Data\n\n");
            inputs.forEach((key, value) ->
                content.append("- **").append(key).append(":** ").append(value).append("\n")
            );
            content.append("\n");
        }

        content.append("## Analysis\n\n");
        content.append("Based on the provided parameters, here is a preliminary analysis:\n\n");
        content.append("1. **Key Findings:** The data suggests opportunities for optimization.\n");
        content.append("2. **Recommendations:** Consider reviewing the key metrics periodically.\n");
        content.append("3. **Next Steps:** Implement the suggested improvements and monitor results.\n\n");

        content.append("## Data Visualizations\n\n");
        content.append("The following charts illustrate key metrics from your data:\n\n");

        // Generate demo charts
        generateDemoCharts(report);

        content.append("---\n\n");
        content.append("*Note: This is a demo report. Connect OpenAI API for AI-generated content with dynamic charts.*\n");

        return content.toString();
    }

    private void generateDemoCharts(Report report) {
        // Create a demo pie chart
        ReportChart pieChart = new ReportChart();
        pieChart.setReport(report);
        pieChart.setChartType("pie");
        pieChart.setTitle("Distribution by Category");
        pieChart.setDataJson("{\"labels\":[\"Category A\",\"Category B\",\"Category C\",\"Category D\"],\"datasets\":[{\"data\":[30,25,20,25],\"backgroundColor\":[\"#5fcfee\",\"#9fb3f5\",\"#e9a9c4\",\"#fde2b8\"]}]}");
        pieChart.setSortOrder(0);
        chartRepository.save(pieChart);

        // Create a demo bar chart
        ReportChart barChart = new ReportChart();
        barChart.setReport(report);
        barChart.setChartType("bar");
        barChart.setTitle("Monthly Trend Analysis");
        barChart.setDataJson("{\"labels\":[\"Jan\",\"Feb\",\"Mar\",\"Apr\",\"May\",\"Jun\"],\"datasets\":[{\"label\":\"Value\",\"data\":[65,59,80,81,56,55],\"backgroundColor\":\"#5fcfee\"}]}");
        barChart.setSortOrder(1);
        chartRepository.save(barChart);

        // Create a demo line chart
        ReportChart lineChart = new ReportChart();
        lineChart.setReport(report);
        lineChart.setChartType("line");
        lineChart.setTitle("Growth Projection");
        lineChart.setDataJson("{\"labels\":[\"Q1\",\"Q2\",\"Q3\",\"Q4\"],\"datasets\":[{\"label\":\"Projected\",\"data\":[100,120,145,180],\"borderColor\":\"#5fcfee\",\"fill\":false},{\"label\":\"Actual\",\"data\":[100,115,140,null],\"borderColor\":\"#e9a9c4\",\"fill\":false}]}");
        lineChart.setSortOrder(2);
        chartRepository.save(lineChart);
    }

    /**
     * Fixes malformed Markdown table separator rows produced by LLMs.
     * Replaces lines like | -- | - | or |--|| with properly spaced separators
     * that match the column count of the header row immediately above them.
     */
    private String sanitizeMarkdownTables(String markdown) {
        if (markdown == null || markdown.isBlank()) return markdown;

        String[] lines = markdown.split("\n", -1);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Detect a separator row: starts/ends with | and contains only |, -, spaces
            if (line.matches("\\s*\\|[\\s|\\-]*\\|\\s*")) {

                // Count columns from the PREVIOUS header line
                int cols = 2; // fallback
                if (i > 0) {
                    String header = lines[i - 1].trim();
                    if (header.startsWith("|") && header.endsWith("|")) {
                        // Count pipes minus the two boundary ones
                        cols = header.split("\\|", -1).length - 2;
                        if (cols < 1) cols = 2;
                    }
                }

                // Rebuild a clean separator row
                StringBuilder sep = new StringBuilder("|");
                for (int c = 0; c < cols; c++) {
                    sep.append(" --- |");
                }
                result.append(sep).append("\n");

            } else {
                result.append(line).append("\n");
            }
        }

        // Trim trailing newline added by the loop
        String out = result.toString();
        if (out.endsWith("\n") && !markdown.endsWith("\n")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    /* -------------------------
       MAPPER
    ------------------------- */
    private ReportResponse toResponse(Report report) {
        // Get charts for this report
        List<ReportResponse.ChartData> charts = chartRepository.findByReportOrderBySortOrderAsc(report)
                .stream()
                .map(c -> new ReportResponse.ChartData(
                        c.getId(),
                        c.getChartType(),
                        c.getTitle(),
                        c.getDataJson(),
                        c.getOptionsJson()
                ))
                .collect(Collectors.toList());

        // Get uploaded files for this report
        List<ReportResponse.UploadedFileInfo> files = uploadedFileRepository.findByReport(report)
                .stream()
                .map(f -> new ReportResponse.UploadedFileInfo(
                        f.getId(),
                        f.getOriginalFilename(),
                        f.getContentType(),
                        f.getFileSize(),
                        f.getExtractedDataJson() != null ? "Data extracted" : "Text extracted"
                ))
                .collect(Collectors.toList());

        return new ReportResponse(
                report.getId(),
                report.getToolId(),
                report.getTitle(),
                report.getStatus().name(),
                report.getContent(),
                report.getCreatedAt(),
                report.getIndustry(),
                report.getReportType(),
                report.getAudience(),
                report.getPurpose(),
                report.getTone(),
                report.getDepth(),
                charts,
                files
        );
    }
}



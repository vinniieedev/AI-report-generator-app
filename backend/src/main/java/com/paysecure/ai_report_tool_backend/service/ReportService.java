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
        ReportTemplate template = templateRepository.save(
                ReportTemplate.builder()
                        .toolId(report.getToolId())
                        .inputFields(List.of())
                        .build()
        );

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
    public ReportResponse generate(UUID reportId, User user) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));

        // Check ownership
        if (!report.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        // Check credits
        if (!creditService.hasEnoughCredits(user, CREDITS_PER_REPORT)) {
            throw new ApiException("Insufficient credits. Please purchase more credits.", HttpStatus.PAYMENT_REQUIRED);
        }

        // Update status
        report.setStatus(ReportStatus.PROCESSING);
        reportRepository.save(report);

        try {
            // Get prompt config
            String systemPrompt = null;
            String userPrompt = "Generate a detailed " + report.getReportType() + " report for the " + report.getIndustry() + " industry.";
            
            if (report.getTemplate() != null) {
                promptConfigRepository.findByTemplateAndIsActiveTrue(report.getTemplate())
                        .ifPresent(config -> {

                        });
            }

            // Get inputs as map
            Map<String, String> inputs = new HashMap<>();
            report.getInputs().forEach(input -> 
                inputs.put(input.getFieldKey(), input.getValue())
            );

            // Generate with AI
            String content;
            log.info("OpenAIService is enabled : "+ openAIService.isConfigured());
            if (openAIService.isConfigured()) {
                content = openAIService.generateReport(report, user, systemPrompt, userPrompt, inputs);
                report.setAiModel("gpt-4o");
            } else {
                // Fallback: generate mock content
                content = generateMockReport(report, inputs);
                report.setAiModel("mock");
            }

            // Deduct credits
            creditService.deductCredits(
                    user,
                    CREDITS_PER_REPORT,
                    TransactionType.REPORT_USAGE,
                    reportId.toString(),
                    "Generated report: " + report.getTitle()
            );

            report.setContent(content);
            report.setStatus(ReportStatus.GENERATED);
            report.setCreditsUsed(CREDITS_PER_REPORT);
            report.setCompletedAt(Instant.now());

        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setContent("Report generation failed: " + e.getMessage());
            reportRepository.save(report);
            throw new ApiException("Report generation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
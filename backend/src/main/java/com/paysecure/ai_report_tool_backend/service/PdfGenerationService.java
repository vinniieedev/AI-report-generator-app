package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.ReportChart;
import com.paysecure.ai_report_tool_backend.repository.ReportChartRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.properties.UnitValue;
import com.paysecure.ai_report_tool_backend.utils.MarkdownUtils;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGenerationService {

    private final ReportChartRepository chartRepository;

    // Brand colors
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(95, 207, 238);  // #5fcfee
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(159, 179, 245); // #9fb3f5
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(233, 169, 196);   // #e9a9c4
    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationService.class);

    public PdfGenerationService(ReportChartRepository chartRepository) {
        this.chartRepository = chartRepository;
    }

    public byte[] generatePdf(Report report) {
        logger.info("Starting PDF generation for reportId={}, title={}", report.getId(), report.getTitle());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            addHeader(document, report);
            addMetadataSection(document, report);
            addContent(document, pdf, report);

            List<ReportChart> charts = chartRepository.findByReportOrderBySortOrderAsc(report);
            if (!charts.isEmpty()) {
                addChartsSection(document, charts);
            }

            addFooter(document);

            document.close();

            logger.info("PDF generation completed successfully for reportId={}", report.getId());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF for reportId={}, error={}", report.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, Report report) {

        document.add(new Paragraph(report.getTitle())
                .setFontSize(26)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        document.add(new Paragraph(report.getReportType() + " | " + report.getIndustry())
                .setFontSize(13)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(25));
    }

    private void addMetadataSection(Document document, Report report) {
        // Create a table for metadata
        Table metaTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Row 1
        metaTable.addCell(createMetaCell("Audience", report.getAudience()));
        metaTable.addCell(createMetaCell("Purpose", report.getPurpose()));
        metaTable.addCell(createMetaCell("Tone", report.getTone()));

        // Row 2
        metaTable.addCell(createMetaCell("Depth", report.getDepth()));
        metaTable.addCell(createMetaCell("Generated", 
                report.getCreatedAt() != null ? 
                        DateTimeFormatter.ofPattern("MMM dd, yyyy").format(
                                report.getCreatedAt().atZone(java.time.ZoneId.systemDefault())) : "N/A"));
        metaTable.addCell(createMetaCell("Status", report.getStatus().name()));

        document.add(metaTable);
    }

    private Cell createMetaCell(String label, String value) {
        Cell cell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10);

        cell.add(new Paragraph(label)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(value != null ? value : "N/A")
                .setFontSize(12)
                .setBold());

        return cell;
    }

    private void addContent(Document document, PdfDocument pdf, Report report) {

        if (report.getContent() == null || report.getContent().isEmpty()) {
            logger.warn("Report content is empty for reportId={}", report.getId());
            document.add(new Paragraph("No content available.")
                    .setFontColor(ColorConstants.GRAY));
            return;
        }

        try {
            String markdown = report.getContent();
            String htmlBody = MarkdownUtils.toHtml(markdown);

            logger.info("HTML content length for reportId={} is {}", report.getId(), htmlBody.length());

            String styledHtml = """
            <style>
                body {
                    font-family: Helvetica, Arial, sans-serif;
                    font-size: 11pt;
                    line-height: 1.6;
                    color: #333333;
                }
                h1 {
                    color: #5fcfee;
                    font-size: 22px;
                    margin-top: 20px;
                }
                h2 {
                    color: #9fb3f5;
                    font-size: 18px;
                    margin-top: 18px;
                }
                h3 {
                    font-size: 14px;
                    margin-top: 15px;
                }
                p {
                    margin-bottom: 10px;
                }
                table {
                    border-collapse: collapse;
                    width: 100%;
                    margin-top: 10px;
                    margin-bottom: 20px;
                }
                table, th, td {
                    border: 1px solid #dddddd;
                }
                th {
                    background-color: #5fcfee;
                    color: white;
                    padding: 6px;
                }
                td {
                    padding: 6px;
                }
                ul {
                    margin-left: 20px;
                }
                blockquote {
                    background: #f5f5f5;
                    padding: 10px;
                    border-left: 4px solid #5fcfee;
                }
            </style>
        """ + htmlBody;

            List<IElement> elements = HtmlConverter.convertToElements(styledHtml);

            Div container = new Div();

            for (IElement element : elements) {
                if (element instanceof IBlockElement) {
                    container.add((IBlockElement) element);
                }
            }

            document.add(container);

            logger.info("Markdown successfully rendered into PDF for reportId={}", report.getId());

        } catch (Exception e) {
            logger.error("Error rendering content for reportId={}, error={}", report.getId(), e.getMessage(), e);

            document.add(new Paragraph("Failed to render report content.")
                    .setFontColor(ColorConstants.RED));
        }
    }

    private void addChartsSection(Document document, List<ReportChart> charts) {

        document.add(new AreaBreak());

        document.add(new Paragraph("Financial Data Visualizations")
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(20));

        for (ReportChart chart : charts) {

            document.add(new Paragraph(chart.getTitle())
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));

            try {
                com.google.gson.JsonObject chartData =
                        new com.google.gson.Gson().fromJson(chart.getDataJson(),
                                com.google.gson.JsonObject.class);

                com.google.gson.JsonArray labels = chartData.getAsJsonArray("labels");
                com.google.gson.JsonArray datasets = chartData.getAsJsonArray("datasets");

                if (labels == null || datasets == null || datasets.size() == 0) {
                    logger.warn("Invalid chart data for chartId={}", chart.getId());
                    continue;
                }

                com.google.gson.JsonObject dataset = datasets.get(0).getAsJsonObject();
                com.google.gson.JsonArray values = dataset.getAsJsonArray("data");

                Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2}))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginBottom(25);

                table.addHeaderCell(createPremiumHeader("Category"));
                table.addHeaderCell(createPremiumHeader("Amount"));

                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i).getAsString();
                    double value = values.get(i).getAsDouble();

                    table.addCell(createPremiumCell(label));
                    table.addCell(createPremiumCell(formatCurrency(value))
                            .setTextAlignment(TextAlignment.RIGHT));
                }

                document.add(table);

            } catch (Exception e) {
                logger.error("Error rendering chartId={}, error={}", chart.getId(), e.getMessage(), e);

                document.add(new Paragraph("Unable to render visualization data.")
                        .setFontColor(ColorConstants.RED));
            }
        }
    }

    private Cell createPremiumHeader(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(PRIMARY_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8);
    }

    private Cell createPremiumCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(6);
    }

    private String formatCurrency(double value) {
        return String.format("₹%,.0f", value);
    }

    private Cell createTableHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(PRIMARY_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8);
    }

    private Cell createTableCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(6);
    }

    private void addFooter(Document document) {

        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setMarginTop(40));

        document.add(new Paragraph("Confidential Financial Analysis Report")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

        document.add(new Paragraph("Generated by Report Wizard")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }
}

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
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

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

    public PdfGenerationService(ReportChartRepository chartRepository) {
        this.chartRepository = chartRepository;
    }

    public byte[] generatePdf(Report report) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // Add header
            addHeader(document, report);

            // Add metadata section
            addMetadataSection(document, report);

            // Add main content
            addContent(document, report);

            // Add charts section
            List<ReportChart> charts = chartRepository.findByReportOrderBySortOrderAsc(report);
            if (!charts.isEmpty()) {
                addChartsSection(document, charts);
            }

            // Add footer
            addFooter(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document document, Report report) {
        // Title
        Paragraph title = new Paragraph(report.getTitle())
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(title);

        // Subtitle with report type
        Paragraph subtitle = new Paragraph(report.getReportType() + " | " + report.getIndustry())
                .setFontSize(14)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);

        // Divider line
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
                .setMarginBottom(20));
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

    private void addContent(Document document, Report report) {
        if (report.getContent() == null || report.getContent().isEmpty()) {
            document.add(new Paragraph("No content available.")
                    .setFontColor(ColorConstants.GRAY));
            return;
        }

        // Parse markdown-like content
        String content = report.getContent();
        String[] lines = content.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                document.add(new Paragraph(" ").setMarginBottom(5));
                continue;
            }

            if (line.startsWith("# ")) {
                // H1
                document.add(new Paragraph(line.substring(2))
                        .setFontSize(20)
                        .setBold()
                        .setFontColor(PRIMARY_COLOR)
                        .setMarginTop(15)
                        .setMarginBottom(10));
            } else if (line.startsWith("## ")) {
                // H2
                document.add(new Paragraph(line.substring(3))
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(SECONDARY_COLOR)
                        .setMarginTop(12)
                        .setMarginBottom(8));
            } else if (line.startsWith("### ")) {
                // H3
                document.add(new Paragraph(line.substring(4))
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(6));
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                // Bullet point
                document.add(new Paragraph("• " + line.substring(2))
                        .setMarginLeft(20)
                        .setMarginBottom(3));
            } else if (line.matches("^\\d+\\..*")) {
                // Numbered list
                document.add(new Paragraph(line)
                        .setMarginLeft(20)
                        .setMarginBottom(3));
            } else if (line.startsWith("> ")) {
                // Blockquote
                document.add(new Paragraph(line.substring(2))
                        .setBackgroundColor(new DeviceRgb(245, 245, 245))
                        .setPadding(10)
                        .setItalic()
                        .setMarginBottom(5));
            } else if (line.startsWith("---")) {
                // Horizontal rule
                document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                        .setMarginTop(10)
                        .setMarginBottom(10));
            } else if (line.startsWith("**") && line.endsWith("**")) {
                // Bold
                document.add(new Paragraph(line.substring(2, line.length() - 2))
                        .setBold()
                        .setMarginBottom(5));
            } else {
                // Regular paragraph
                document.add(new Paragraph(line)
                        .setMarginBottom(5));
            }
        }
    }

    private void addChartsSection(Document document, List<ReportChart> charts) {
        document.add(new AreaBreak());

        document.add(new Paragraph("Data Visualizations")
                .setFontSize(18)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(15));

        document.add(new Paragraph("The following charts provide visual insights from your data:")
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20));

        for (ReportChart chart : charts) {
            // Chart title
            document.add(new Paragraph(chart.getTitle())
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(10));

            // Chart type badge
            document.add(new Paragraph("Chart Type: " + chart.getChartType().toUpperCase())
                    .setFontSize(10)
                    .setFontColor(SECONDARY_COLOR)
                    .setMarginBottom(10));

            // Note about charts
            document.add(new Paragraph("Note: Interactive charts are available in the web application.")
                    .setFontSize(10)
                    .setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setBackgroundColor(new DeviceRgb(250, 250, 250))
                    .setPadding(10)
                    .setMarginBottom(20));
        }
    }

    private void addFooter(Document document) {
        document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setMarginTop(30));

        Paragraph footer = new Paragraph()
                .add("Generated by Report Wizard | ")
                .add("AI-Powered Financial Reports")
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);

        document.add(footer);

        Paragraph disclaimer = new Paragraph(
                "This report is generated for informational purposes only. Please consult with qualified " +
                        "professionals before making any financial decisions.")
                .setFontSize(8)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);

        document.add(disclaimer);
    }
}

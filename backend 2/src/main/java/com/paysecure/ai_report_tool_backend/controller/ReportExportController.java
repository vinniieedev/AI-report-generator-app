package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.PdfGenerationService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportExportController {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final PdfGenerationService pdfGenerationService;

    public ReportExportController(
            ReportRepository reportRepository,
            UserService userService,
            PdfGenerationService pdfGenerationService
    ) {
        this.reportRepository = reportRepository;
        this.userService = userService;
        this.pdfGenerationService = pdfGenerationService;
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));

        // Check ownership
        if (!report.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        byte[] pdfBytes = pdfGenerationService.generatePdf(report);

        String filename = report.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_") + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/export/markdown")
    public ResponseEntity<byte[]> exportToMarkdown(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ApiException("Report not found", HttpStatus.NOT_FOUND));

        if (!report.getUser().getId().equals(user.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        String content = report.getContent() != null ? report.getContent() : "";
        byte[] mdBytes = content.getBytes();

        String filename = report.getTitle().replaceAll("[^a-zA-Z0-9-_]", "_") + ".md";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_MARKDOWN);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(mdBytes.length);

        return new ResponseEntity<>(mdBytes, headers, HttpStatus.OK);
    }
}

package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.file.FileUploadResponse;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.FileParserService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileParserService fileParserService;
    private final UserService userService;
    private final ReportRepository reportRepository;

    public FileUploadController(
            FileParserService fileParserService,
            UserService userService,
            ReportRepository reportRepository
    ) {
        this.fileParserService = fileParserService;
        this.userService = userService;
        this.reportRepository = reportRepository;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "reportId", required = false) UUID reportId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        Report report = null;
        if (reportId != null) {
            report = reportRepository.findById(reportId).orElse(null);
        }

        return fileParserService.uploadAndParse(file, user, report);
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<FileUploadResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "reportId", required = false) UUID reportId
    ) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        Report report = null;
        if (reportId != null) {
            report = reportRepository.findById(reportId).orElse(null);
        }

        final Report finalReport = report;
        return java.util.Arrays.stream(files)
                .map(file -> fileParserService.uploadAndParse(file, user, finalReport))
                .toList();
    }
}

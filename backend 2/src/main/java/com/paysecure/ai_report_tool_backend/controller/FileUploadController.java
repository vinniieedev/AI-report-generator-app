package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.file.FileUploadResponse;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.Report;
import com.paysecure.ai_report_tool_backend.model.UploadedFile;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
import com.paysecure.ai_report_tool_backend.repository.UploadedFileRepository;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.FileParserService;
import com.paysecure.ai_report_tool_backend.service.SignedUrlService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileParserService fileParserService;
    private final UserService userService;
    private final ReportRepository reportRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final SignedUrlService signedUrlService;

    public FileUploadController(
            FileParserService fileParserService,
            UserService userService,
            ReportRepository reportRepository,
            UploadedFileRepository uploadedFileRepository,
            SignedUrlService signedUrlService
    ) {
        this.fileParserService = fileParserService;
        this.userService = userService;
        this.reportRepository = reportRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.signedUrlService = signedUrlService;
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

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadWithToken(
            @RequestParam String token
    ) {

        if (!signedUrlService.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String fileId = signedUrlService.extractFileId(token);

        UploadedFile file = uploadedFileRepository.findById(UUID.fromString(fileId))
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path path = Paths.get(file.getStoragePath());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(resource);
    }

    @GetMapping("/{fileId}/signed-url")
    public Map<String, String> getSignedUrl(@PathVariable UUID fileId) {

        UUID userId = SecurityUtils.getCurrentUserId();

        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        String token = signedUrlService.generateToken(
                file.getId().toString(),
                userId.toString()
        );

        String url = "http://localhost:8080/api/files/download?token=" + token;

        return Map.of("url", url);
    }

}

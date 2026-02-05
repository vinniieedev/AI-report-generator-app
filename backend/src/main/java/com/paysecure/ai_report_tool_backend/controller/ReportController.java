package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.CreateReportRequest;
import com.paysecure.ai_report_tool_backend.dto.ReportResponse;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.ReportService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    ReportService reportService;

    @Autowired
    UserService userService;

    @PostMapping
    public ReportResponse create(@RequestBody CreateReportRequest req) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return reportService.create(req, user);
    }

    @PostMapping("/{id}/generate")
    public ReportResponse generate(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return reportService.generate(id, user);
    }

    @GetMapping
    public List<ReportResponse> myReports() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return reportService.getUserReports(user);
    }

    @GetMapping("/{id}")
    public ReportResponse getReport(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return reportService.getReport(id, user);
    }
}
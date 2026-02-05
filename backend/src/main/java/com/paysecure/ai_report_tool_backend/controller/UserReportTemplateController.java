package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.user.UserReportTemplateResponse;
import com.paysecure.ai_report_tool_backend.service.UserReportTemplateService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report-templates")
@PreAuthorize("isAuthenticated()")
public class UserReportTemplateController {

    private final UserReportTemplateService service;

    public UserReportTemplateController(
            UserReportTemplateService service
    ) {
        this.service = service;
    }

    @GetMapping("/{toolId}")
    public UserReportTemplateResponse getTemplate(
            @PathVariable String toolId
    ) {
        return service.getByToolId(toolId);
    }
}
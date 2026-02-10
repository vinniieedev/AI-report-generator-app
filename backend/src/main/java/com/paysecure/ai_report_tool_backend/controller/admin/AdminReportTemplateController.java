package com.paysecure.ai_report_tool_backend.controller.admin;

import com.paysecure.ai_report_tool_backend.dto.admin.*;
import com.paysecure.ai_report_tool_backend.service.AdminReportTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/report-templates")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportTemplateController {

    private final AdminReportTemplateService service;

    public AdminReportTemplateController(
            AdminReportTemplateService service
    ) {
        this.service = service;
    }

    /* -------------------------
       Templates
    ------------------------- */

    @GetMapping
    public List<ReportTemplateResponse> all() {
        return service.getAllTemplates();
    }

    @GetMapping("/{id}")
    public ReportTemplateResponse one(@PathVariable UUID id) {
        return service.getTemplate(id);
    }

    /* -------------------------
       Input Fields
    ------------------------- */

    @PostMapping("/{id}/input-fields")
    public InputFieldResponse addField(
            @PathVariable UUID id,
            @RequestBody InputFieldRequest req
    ) {
        return service.addInputField(id, req);
    }

    @PutMapping("/input-fields/{fieldId}")
    public InputFieldResponse updateField(
            @PathVariable UUID fieldId,
            @RequestBody InputFieldRequest req
    ) {
        return service.updateInputField(fieldId, req);
    }

    @DeleteMapping("/input-fields/{fieldId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInputField(@PathVariable UUID fieldId) {
        service.deleteInputField(fieldId);
    }
}
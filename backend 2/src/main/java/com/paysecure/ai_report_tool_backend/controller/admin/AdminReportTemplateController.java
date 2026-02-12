package com.paysecure.ai_report_tool_backend.controller.admin;

import com.paysecure.ai_report_tool_backend.dto.*;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.InputField;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.InputFieldRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/report-templates")
@RequiredArgsConstructor
public class AdminReportTemplateController {

    private final ReportTemplateRepository templateRepository;
    private final InputFieldRepository inputFieldRepository;

    /* =====================================================
       GET ALL
    ===================================================== */

    @GetMapping
    public List<ReportTemplateResponse> getAll() {
        return templateRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* =====================================================
       GET SINGLE
    ===================================================== */

    @GetMapping("/{toolId}")
    public ReportTemplateResponse getByToolId(@PathVariable String toolId) {
        ReportTemplate template = templateRepository.findByToolId(toolId)
                .orElseThrow(() ->
                        new ApiException("Template not found", HttpStatus.NOT_FOUND));

        return mapToResponse(template);
    }

    /* =====================================================
       CREATE TEMPLATE
    ===================================================== */

    @PostMapping
    public ReportTemplateResponse create(
            @Valid @RequestBody ReportTemplateRequest request
    ) {

        String normalizedToolId = request.toolId().trim().toLowerCase();

        if (templateRepository.existsByToolId(normalizedToolId)) {
            throw new ApiException("ToolId already exists", HttpStatus.BAD_REQUEST);
        }

        ReportTemplate template = ReportTemplate.builder()
                .toolId(normalizedToolId)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .industry(request.industry())
                .systemPrompt(request.systemPrompt())
                .calculationPrompt(request.calculationPrompt())
                .outputFormatPrompt(request.outputFormatPrompt())
                .build();

        return mapToResponse(templateRepository.save(template));
    }

    /* =====================================================
       UPDATE TEMPLATE
    ===================================================== */

    @PutMapping("/{toolId}")
    public ReportTemplateResponse update(
            @PathVariable String toolId,
            @Valid @RequestBody ReportTemplateRequest request
    ) {

        ReportTemplate template = templateRepository.findByToolId(toolId)
                .orElseThrow(() ->
                        new ApiException("Template not found", HttpStatus.NOT_FOUND));

        template.setTitle(request.title());
        template.setDescription(request.description());
        template.setCategory(request.category());
        template.setIndustry(request.industry());
        template.setSystemPrompt(request.systemPrompt());
        template.setCalculationPrompt(request.calculationPrompt());
        template.setOutputFormatPrompt(request.outputFormatPrompt());

        return mapToResponse(templateRepository.save(template));
    }

    /* =====================================================
       DELETE TEMPLATE
    ===================================================== */

    @DeleteMapping("/{toolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String toolId) {

        ReportTemplate template = templateRepository.findByToolId(toolId)
                .orElseThrow(() ->
                        new ApiException("Template not found", HttpStatus.NOT_FOUND));

        templateRepository.delete(template);
    }

    /* =====================================================
       MAPPER
    ===================================================== */

    private ReportTemplateResponse mapToResponse(ReportTemplate template) {
        return new ReportTemplateResponse(
                template.getId(),
                template.getToolId(),
                template.getTitle(),
                template.getDescription(),
                template.getCategory(),
                template.getIndustry(),
                template.getSystemPrompt(),
                template.getCalculationPrompt(),
                template.getOutputFormatPrompt(),
                template.getInputFields()
                        .stream()
                        .map(this::mapToFieldResponse)
                        .collect(Collectors.toList())
        );
    }

    private InputFieldResponse mapToFieldResponse(InputField field) {
        return new InputFieldResponse(
                field.getId(),
                field.getLabel(),
                field.getDescription(),
                field.getType().name(),
                field.isRequired(),
                field.getMinValue(),
                field.getMaxValue(),
                field.getOptions(),
                field.getSortOrder()
        );
    }
}
package com.paysecure.ai_report_tool_backend.controller.admin;

import com.paysecure.ai_report_tool_backend.dto.*;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.InputField;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.InputFieldRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportRepository;
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
    private final ReportRepository reportRepository;

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
        System.out.println("#################################################");
        System.out.println(toolId);
        System.out.println(request);
        System.out.println("#################################################");
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
        template.setTemperature(request.temperature());
        template.setMaxTokens(request.maxTokens());
        template.setActive(request.active());

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
        if (reportRepository.existsByTemplate(template)) {
            throw new ApiException(
                    "Cannot delete template. Reports are using this template.",
                    HttpStatus.BAD_REQUEST
            );
        }
        templateRepository.delete(template);
    }

    @PostMapping("/{toolId}/input-fields")
    public InputFieldResponse addField(
            @PathVariable String toolId,
            @Valid @RequestBody InputFieldRequest request
    ) {
        ReportTemplate template = templateRepository.findByToolId(toolId)
                .orElseThrow(() ->
                        new ApiException("Template not found", HttpStatus.NOT_FOUND));

        InputField field = new InputField();
        field.setLabel(request.label());
        field.setDescription(request.description());
        field.setType(request.type());
        field.setRequired(request.required());
        field.setMinValue(request.minValue());
        field.setMaxValue(request.maxValue());
        field.setOptions(request.options());
        field.setSortOrder(request.sortOrder());
        field.setTemplate(template);

        return mapToFieldResponse(inputFieldRepository.save(field));
    }

    /* =====================================================
   UPDATE INPUT FIELD
===================================================== */

    @PutMapping("/input-fields/{fieldId}")
    public InputFieldResponse updateField(
            @PathVariable UUID fieldId,
            @Valid @RequestBody InputFieldRequest request
    ) {
        InputField field = inputFieldRepository.findById(fieldId)
                .orElseThrow(() ->
                        new ApiException("Input field not found", HttpStatus.NOT_FOUND));
        ReportTemplate template = templateRepository.findByToolId(field.getTemplate().getToolId())
                .orElseThrow(() ->
                        new ApiException("Template not found", HttpStatus.NOT_FOUND));



        // Optional safety check: ensure field belongs to this template
        if (!field.getTemplate().getId().equals(template.getId())) {
            throw new ApiException("Input field does not belong to this template", HttpStatus.BAD_REQUEST);
        }

        field.setLabel(request.label());
        field.setDescription(request.description());
        field.setType(request.type());
        field.setRequired(request.required());
        field.setMinValue(request.minValue());
        field.setMaxValue(request.maxValue());
        field.setOptions(request.options());
        field.setSortOrder(request.sortOrder());

        return mapToFieldResponse(inputFieldRepository.save(field));
    }

    /* =====================================================
   DELETE INPUT FIELD
===================================================== */

    @DeleteMapping("/input-fields/{fieldId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteField(@PathVariable UUID fieldId) {

        InputField field = inputFieldRepository.findById(fieldId)
                .orElseThrow(() ->
                        new ApiException("Input field not found", HttpStatus.NOT_FOUND));

        ReportTemplate template = field.getTemplate();

        if (template == null) {
            throw new ApiException("Template not found", HttpStatus.NOT_FOUND);
        }

        // 🔥 THIS IS IMPORTANT
        template.removeInputField(field);

        templateRepository.save(template);
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
                template.getTemperature(),
                template.getMaxTokens(),
                template.getActive(),
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
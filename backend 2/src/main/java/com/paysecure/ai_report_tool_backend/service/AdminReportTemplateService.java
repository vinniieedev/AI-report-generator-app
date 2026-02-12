package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.admin.*;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.InputField;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.InputFieldRepository;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminReportTemplateService {

    private final ReportTemplateRepository templateRepo;
    private final InputFieldRepository fieldRepo;

    public AdminReportTemplateService(
            ReportTemplateRepository templateRepo,
            InputFieldRepository fieldRepo
    ) {
        this.templateRepo = templateRepo;
        this.fieldRepo = fieldRepo;
    }

    /* -------------------------
       Templates
    ------------------------- */

    public List<ReportTemplateResponse> getAllTemplates() {
        return templateRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReportTemplateResponse getTemplate(String id) {
        ReportTemplate template = templateRepo.findByToolId(id)
                .orElseThrow(() ->
                        new ApiException("Template not found with toolId: " + id, HttpStatus.NOT_FOUND)
                );

        return toResponse(template);
    }

    /* -------------------------
       Input Fields
    ------------------------- */

    public InputFieldResponse addInputField(
            UUID templateId,
            InputFieldRequest req
    ) {
        ReportTemplate template = templateRepo.findById(templateId)
                .orElseThrow();

        InputField field = new InputField();
        try{
            apply(field, req);
            template.addInputField(field);

            templateRepo.save(template);
        }catch (Exception e){
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        return toResponse(field);
    }

    public InputFieldResponse updateInputField(
            UUID fieldId,
            InputFieldRequest req
    ) {
        InputField field = fieldRepo.findById(fieldId)
                .orElseThrow();

        apply(field, req);
        return toResponse(fieldRepo.save(field));
    }

    public void deleteInputField(UUID fieldId) {
        InputField field = fieldRepo.findById(fieldId)
                .orElseThrow();

        ReportTemplate template = field.getTemplate();

        template.removeInputField(field);

        templateRepo.save(template);
    }

    /* -------------------------
       Mapping Helpers
    ------------------------- */

    private void apply(InputField field, InputFieldRequest req) {
        field.setLabel(req.label());
        field.setDescription(req.description());
        field.setType(req.type());
        field.setRequired(req.required());
        field.setMinValue(req.minValue());
        field.setMaxValue(req.maxValue());
        field.setOptions(req.options());
        field.setSortOrder(req.sortOrder());
    }

    private ReportTemplateResponse toResponse(ReportTemplate t) {
        return new ReportTemplateResponse(
                t.getId(),
                t.getToolId(),
                t.getTitle(),
                t.getDescription(),
                t.getCategory(),
                t.getIndustry(),
                t.getInputFields().stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private InputFieldResponse toResponse(InputField f) {
        return new InputFieldResponse(
                f.getId(),
                f.getLabel(),
                f.getDescription(),
                f.getType(),
                f.isRequired(),
                f.getMinValue(),
                f.getMaxValue(),
                f.getOptions(),
                f.getSortOrder()
        );
    }
}
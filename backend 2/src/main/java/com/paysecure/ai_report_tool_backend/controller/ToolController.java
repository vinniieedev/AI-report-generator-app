package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.InputFieldResponse;
import com.paysecure.ai_report_tool_backend.dto.ToolResponse;
import com.paysecure.ai_report_tool_backend.model.InputField;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ReportTemplateRepository templateRepository;

    /* =====================================================
       READ ENDPOINTS
    ===================================================== */

    @GetMapping
    public List<ToolResponse> getAllTools() {
        return templateRepository.findAll()
                .stream()
                .map(this::mapToToolResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{toolId}")
    public ToolResponse getTool(@PathVariable String toolId) {
        return templateRepository.findByToolId(toolId)
                .map(this::mapToToolResponse)
                .orElseThrow(() -> new RuntimeException("Tool not found"));
    }

    @GetMapping("/{toolId}/fields")
    public List<InputFieldResponse> getToolFields(@PathVariable String toolId) {
        return templateRepository.findByToolId(toolId)
                .map(template -> template.getInputFields()
                        .stream()
                        .map(this::mapToFieldResponse)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /* =====================================================
       CREATE TEMPLATE
    ===================================================== */

    @PostMapping
    public ToolResponse createTemplate(@RequestBody ReportTemplate template) {

        if (templateRepository.existsByToolId(template.getToolId())) {
            throw new RuntimeException("ToolId already exists");
        }

        template.getInputFields()
                .forEach(field -> field.setTemplate(template));

        ReportTemplate saved = templateRepository.save(template);

        return mapToToolResponse(saved);
    }

    /* =====================================================
       UPDATE TEMPLATE
    ===================================================== */

    @PutMapping("/{toolId}")
    public ToolResponse updateTemplate(
            @PathVariable String toolId,
            @RequestBody ReportTemplate updatedTemplate
    ) {

        ReportTemplate existing = templateRepository.findByToolId(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        existing.setTitle(updatedTemplate.getTitle());
        existing.setDescription(updatedTemplate.getDescription());
        existing.setCategory(updatedTemplate.getCategory());
        existing.setIndustry(updatedTemplate.getIndustry());

        // Clear and replace input fields
        existing.getInputFields().clear();

        updatedTemplate.getInputFields().forEach(field -> {
            field.setTemplate(existing);
            existing.getInputFields().add(field);
        });

        ReportTemplate saved = templateRepository.save(existing);

        return mapToToolResponse(saved);
    }

    /* =====================================================
       DELETE TEMPLATE
    ===================================================== */

    @DeleteMapping("/{toolId}")
    public void deleteTemplate(@PathVariable String toolId) {

        ReportTemplate template = templateRepository.findByToolId(toolId)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        templateRepository.delete(template);
    }

    /* =====================================================
       MAPPERS
    ===================================================== */

    private ToolResponse mapToToolResponse(ReportTemplate t) {
        return new ToolResponse(
                t.getToolId(),
                t.getTitle(),
                t.getDescription(),
                t.getCategory(),
                t.getIndustry(),
                t.getInputFields()
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
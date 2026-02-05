package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.user.*;
import com.paysecure.ai_report_tool_backend.model.ReportTemplate;
import com.paysecure.ai_report_tool_backend.repository.ReportTemplateRepository;
import org.springframework.stereotype.Service;

@Service
public class UserReportTemplateService {

    private final ReportTemplateRepository templateRepo;

    public UserReportTemplateService(
            ReportTemplateRepository templateRepo
    ) {
        this.templateRepo = templateRepo;
    }

    public UserReportTemplateResponse getByToolId(String toolId) {
        ReportTemplate template = templateRepo.findByToolId(toolId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Report template not found")
                );

        return new UserReportTemplateResponse(
                template.getToolId(),
                template.getTitle(),
                template.getDescription(),
                template.getCategory(),
                template.getIndustry(),
                template.getInputFields().stream()
                        .map(f -> new UserInputFieldResponse(
                                f.getId(),
                                f.getLabel(),
                                f.getDescription(),
                                f.getType(),
                                f.isRequired(),
                                f.getMinValue(),
                                f.getMaxValue(),
                                f.getOptions()
                        ))
                        .toList()
        );
    }
}
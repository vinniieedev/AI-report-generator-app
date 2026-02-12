package com.paysecure.ai_report_tool_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReportTemplateRequest(

        @NotBlank(message = "Tool ID is required")
        String toolId,

        @NotBlank(message = "Title is required")
        String title,

        @Size(max = 1000)
        String description,

        String category,
        String industry,

        String systemPrompt,
        String calculationPrompt,
        String outputFormatPrompt,

        Double temperature,
        Integer maxTokens,
        Boolean active,

        List<InputFieldRequest> inputFields
) {}
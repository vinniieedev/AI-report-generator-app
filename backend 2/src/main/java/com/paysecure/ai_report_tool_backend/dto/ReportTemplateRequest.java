package com.paysecure.ai_report_tool_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReportTemplateRequest(

        @NotBlank(message = "Tool ID is required")
        String toolId,

        @NotBlank(message = "Title is required")
        String title,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        String category,
        String industry,

        String systemPrompt,
        String calculationPrompt,
        String outputFormatPrompt,

        List<InputFieldRequest> inputFields
) {}
package com.paysecure.ai_report_tool_backend.dto;

import java.util.List;
import java.util.UUID;

public record ReportTemplateResponse(
        UUID id,
        String toolId,
        String title,
        String description,
        String category,
        String industry,
        String systemPrompt,
        String calculationPrompt,
        String outputFormatPrompt,
        Double temperature,
        Integer maxTokens,
        Boolean active,

        List<InputFieldResponse> inputFields
) {}
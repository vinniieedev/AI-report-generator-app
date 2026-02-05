package com.paysecure.ai_report_tool_backend.dto.admin;

import java.util.List;
import java.util.UUID;

public record ReportTemplateResponse(
        UUID id,
        String toolId,
        String title,
        String description,
        String category,
        String industry,
        List<InputFieldResponse> inputFields
) {}
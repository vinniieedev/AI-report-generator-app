package com.paysecure.ai_report_tool_backend.dto.user;

import java.util.List;

public record UserReportTemplateResponse(
        String toolId,
        String title,
        String description,
        String category,
        String industry,
        List<UserInputFieldResponse> inputFields
) {}
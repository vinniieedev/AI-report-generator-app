package com.paysecure.ai_report_tool_backend.dto;

import java.util.Map;

public record CreateReportRequest(
        String tool_id,
        String title,
        String industry,
        String report_type,
        String audience,
        String purpose,
        String tone,
        String depth,
        Map<String, Object> wizard_data,
        Map<String, String> inputs  // Dynamic input fields from template
) {}
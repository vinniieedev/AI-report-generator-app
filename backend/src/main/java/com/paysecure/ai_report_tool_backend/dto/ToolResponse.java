package com.paysecure.ai_report_tool_backend.dto;

public record ToolResponse(
        String id,
        String title,
        String description,
        String category,
        String industry
) {}

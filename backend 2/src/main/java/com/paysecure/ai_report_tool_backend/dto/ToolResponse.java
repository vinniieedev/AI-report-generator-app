package com.paysecure.ai_report_tool_backend.dto;

import com.paysecure.ai_report_tool_backend.model.InputField;

import java.util.List;

public record ToolResponse(
        String id,
        String title,
        String description,
        String category,
        String industry,
        List<InputFieldResponse> inputFields
) {}

package com.paysecure.ai_report_tool_backend.dto.user;

import com.paysecure.ai_report_tool_backend.model.enums.InputFieldType;

import java.util.List;
import java.util.UUID;

public record UserInputFieldResponse(
        UUID id,
        String label,
        String description,
        InputFieldType type,
        boolean required,
        Integer minValue,
        Integer maxValue,
        List<String> options
) {}
package com.paysecure.ai_report_tool_backend.dto.admin;

import com.paysecure.ai_report_tool_backend.model.enums.InputFieldType;

import java.util.List;

public record InputFieldRequest(
        String label,
        String description,
        InputFieldType type,
        boolean required,
        Integer minValue,
        Integer maxValue,
        List<String> options,
        int sortOrder
) {}
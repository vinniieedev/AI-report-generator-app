package com.paysecure.ai_report_tool_backend.dto;

import java.util.List;
import java.util.UUID;

public record InputFieldResponse(
        UUID id,
        String label,
        String description,
        String type,
        boolean required,
        Integer minValue,
        Integer maxValue,
        List<String> options,
        int sortOrder
) {}
package com.paysecure.ai_report_tool_backend.dto.payment;

import java.math.BigDecimal;

public record CreditPackage(
        String id,
        String name,
        int credits,
        BigDecimal price,
        String description
) {}

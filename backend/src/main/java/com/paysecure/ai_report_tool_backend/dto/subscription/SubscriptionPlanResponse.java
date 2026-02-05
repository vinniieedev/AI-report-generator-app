package com.paysecure.ai_report_tool_backend.dto.subscription;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionPlanResponse(
        UUID id,
        String name,
        BigDecimal monthlyPrice,
        int creditsPerMonth,
        int maxReportsPerMonth,
        String featuresJson
) {}

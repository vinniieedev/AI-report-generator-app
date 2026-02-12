package com.paysecure.ai_report_tool_backend.dto.subscription;

import java.time.Instant;
import java.util.UUID;

public record UserSubscriptionResponse(
        UUID id,
        String planName,
        String status,
        Instant startDate,
        Instant endDate,
        boolean autoRenew
) {}

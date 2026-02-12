package com.paysecure.ai_report_tool_backend.dto.credit;

import java.time.Instant;
import java.util.UUID;

public record CreditTransactionResponse(
        UUID id,
        String type,
        int credits,
        String referenceId,
        String description,
        int balanceAfter,
        Instant createdAt
) {}

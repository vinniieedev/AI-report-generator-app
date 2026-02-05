package com.paysecure.ai_report_tool_backend.dto.payment;

import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String status,
        String paymentUrl,
        String message
) {}

package com.paysecure.ai_report_tool_backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ReportSummaryResponse(
        UUID id,
        String title,
        String status,
        Instant createdAt
) {}
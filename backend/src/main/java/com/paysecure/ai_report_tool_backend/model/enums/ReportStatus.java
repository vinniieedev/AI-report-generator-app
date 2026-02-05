package com.paysecure.ai_report_tool_backend.model.enums;

public enum ReportStatus {
    DRAFT,
    PENDING,      // Waiting for AI generation
    PROCESSING,   // AI is generating
    GENERATED,
    FAILED
}
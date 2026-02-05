package com.paysecure.ai_report_tool_backend.model.enums;

public enum TransactionType {
    PURCHASE,           // Credit purchase
    REPORT_USAGE,       // Credit used for report generation
    SUBSCRIPTION_GRANT, // Credits from subscription
    REFUND,             // Refund credits
    BONUS,              // Promotional credits
    ADMIN_ADJUSTMENT    // Manual adjustment by admin
}

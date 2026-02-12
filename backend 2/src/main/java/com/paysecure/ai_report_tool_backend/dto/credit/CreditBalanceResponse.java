package com.paysecure.ai_report_tool_backend.dto.credit;

import java.util.List;

public record CreditBalanceResponse(
        int balance,
        List<CreditTransactionResponse> recentTransactions
) {}

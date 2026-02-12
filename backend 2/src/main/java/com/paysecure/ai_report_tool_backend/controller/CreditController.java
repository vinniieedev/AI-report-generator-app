package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.credit.CreditBalanceResponse;
import com.paysecure.ai_report_tool_backend.dto.credit.CreditTransactionResponse;
import com.paysecure.ai_report_tool_backend.model.CreditTransaction;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.CreditService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;
    private final UserService userService;

    public CreditController(
            CreditService creditService,
            UserService userService
    ) {
        this.creditService = creditService;
        this.userService = userService;
    }

    @GetMapping("/balance")
    public CreditBalanceResponse getBalance() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        
        int balance = creditService.getBalance(user);
        List<CreditTransactionResponse> transactions = creditService.getTransactionHistory(user)
                .stream()
                .limit(10)
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new CreditBalanceResponse(balance, transactions);
    }

    @GetMapping("/transactions")
    public List<CreditTransactionResponse> getTransactions() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        
        return creditService.getTransactionHistory(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CreditTransactionResponse toResponse(CreditTransaction t) {
        return new CreditTransactionResponse(
                t.getId(),
                t.getType().name(),
                t.getCredits(),
                t.getReferenceId(),
                t.getDescription(),
                t.getBalanceAfter(),
                t.getCreatedAt()
        );
    }
}

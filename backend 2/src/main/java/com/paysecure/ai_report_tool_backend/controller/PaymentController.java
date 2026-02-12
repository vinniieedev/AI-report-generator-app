package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.payment.CreditPackage;
import com.paysecure.ai_report_tool_backend.dto.payment.PaymentRequest;
import com.paysecure.ai_report_tool_backend.dto.payment.PaymentResponse;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.PaymentService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(
            PaymentService paymentService,
            UserService userService
    ) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/packages")
    public List<CreditPackage> getCreditPackages() {
        return paymentService.getCreditPackages();
    }

    @PostMapping("/purchase")
    public PaymentResponse purchaseCredits(@RequestBody PaymentRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return paymentService.initiatePurchase(user, request);
    }

    @PostMapping("/confirm")
    public PaymentResponse confirmPayment(
            @RequestParam String paymentId,
            @RequestParam String externalPaymentId
    ) {
        return paymentService.confirmPayment(paymentId, externalPaymentId);
    }
}

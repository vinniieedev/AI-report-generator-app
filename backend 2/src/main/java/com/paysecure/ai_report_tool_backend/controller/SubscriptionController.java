package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.subscription.SubscribeRequest;
import com.paysecure.ai_report_tool_backend.dto.subscription.SubscriptionPlanResponse;
import com.paysecure.ai_report_tool_backend.dto.subscription.UserSubscriptionResponse;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.UserSubscription;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.SubscriptionService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public SubscriptionController(
            SubscriptionService subscriptionService,
            UserService userService
    ) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping("/plans")
    public List<SubscriptionPlanResponse> getPlans() {
        return subscriptionService.getActivePlans();
    }

    @GetMapping("/current")
    public UserSubscriptionResponse getCurrentSubscription() {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        return subscriptionService.getCurrentSubscription(user);
    }

    @PostMapping("/subscribe")
    public UserSubscriptionResponse subscribe(@RequestBody SubscribeRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        UserSubscription subscription = subscriptionService.subscribeToPlan(user, request.planId());
        return new UserSubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getName(),
                subscription.getStatus().name(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.isAutoRenew()
        );
    }
}

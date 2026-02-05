package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.dto.subscription.SubscriptionPlanResponse;
import com.paysecure.ai_report_tool_backend.dto.subscription.UserSubscriptionResponse;
import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.SubscriptionPlan;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.UserSubscription;
import com.paysecure.ai_report_tool_backend.model.enums.Plan;
import com.paysecure.ai_report_tool_backend.model.enums.SubscriptionStatus;
import com.paysecure.ai_report_tool_backend.model.enums.TransactionType;
import com.paysecure.ai_report_tool_backend.repository.SubscriptionPlanRepository;
import com.paysecure.ai_report_tool_backend.repository.UserRepository;
import com.paysecure.ai_report_tool_backend.repository.UserSubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;

    public SubscriptionService(
            SubscriptionPlanRepository planRepository,
            UserSubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            CreditService creditService
    ) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.creditService = creditService;
    }

    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserSubscription> getActiveSubscription(User user) {
        return subscriptionRepository.findByUserAndStatus(user, SubscriptionStatus.ACTIVE);
    }

    public UserSubscriptionResponse getCurrentSubscription(User user) {
        return getActiveSubscription(user)
                .map(this::toUserSubscriptionResponse)
                .orElse(null);
    }

    @Transactional
    public UserSubscription subscribeToPlan(User user, UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ApiException("Plan not found", HttpStatus.NOT_FOUND));

        // Cancel existing subscription if any
        getActiveSubscription(user).ifPresent(existing -> {
            existing.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(existing);
        });

        // Create new subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(Instant.now());
        subscription.setEndDate(Instant.now().plus(30, ChronoUnit.DAYS));

        subscriptionRepository.save(subscription);

        // Update user plan
        user.setPlan(Plan.valueOf(plan.getName()));
        userRepository.save(user);

        // Grant credits
        creditService.addCredits(
                user,
                plan.getCreditsPerMonth(),
                TransactionType.SUBSCRIPTION_GRANT,
                subscription.getId().toString(),
                "Monthly credits from " + plan.getName() + " subscription"
        );

        return subscription;
    }

    @Transactional
    public void initializeDefaultPlans() {
        if (planRepository.count() > 0) return;

        // Free Plan
        SubscriptionPlan free = new SubscriptionPlan();
        free.setName("Free");
        free.setMonthlyPrice(java.math.BigDecimal.ZERO);
        free.setCreditsPerMonth(10);
        free.setMaxReportsPerMonth(5);
        free.setFeaturesJson("[\"10 credits/month\", \"5 reports/month\", \"Basic templates\", \"Email support\"]");
        free.setSortOrder(0);
        planRepository.save(free);

        // Pro Plan
        SubscriptionPlan pro = new SubscriptionPlan();
        pro.setName("Pro");
        pro.setMonthlyPrice(java.math.BigDecimal.valueOf(29.99));
        pro.setCreditsPerMonth(100);
        pro.setMaxReportsPerMonth(50);
        pro.setFeaturesJson("[\"100 credits/month\", \"50 reports/month\", \"All templates\", \"Priority support\", \"Custom branding\"]");
        pro.setSortOrder(1);
        planRepository.save(pro);

        // Enterprise Plan
        SubscriptionPlan enterprise = new SubscriptionPlan();
        enterprise.setName("Enterprise");
        enterprise.setMonthlyPrice(java.math.BigDecimal.valueOf(99.99));
        enterprise.setCreditsPerMonth(500);
        enterprise.setMaxReportsPerMonth(999);
        enterprise.setFeaturesJson("[\"500 credits/month\", \"Unlimited reports\", \"All templates\", \"24/7 support\", \"Custom branding\", \"API access\", \"Dedicated account manager\"]");
        enterprise.setSortOrder(2);
        planRepository.save(enterprise);
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getMonthlyPrice(),
                plan.getCreditsPerMonth(),
                plan.getMaxReportsPerMonth(),
                plan.getFeaturesJson()
        );
    }

    private UserSubscriptionResponse toUserSubscriptionResponse(UserSubscription sub) {
        return new UserSubscriptionResponse(
                sub.getId(),
                sub.getPlan().getName(),
                sub.getStatus().name(),
                sub.getStartDate(),
                sub.getEndDate(),
                sub.isAutoRenew()
        );
    }
}

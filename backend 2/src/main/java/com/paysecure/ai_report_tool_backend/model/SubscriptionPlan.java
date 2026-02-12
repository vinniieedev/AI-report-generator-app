package com.paysecure.ai_report_tool_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // Free, Pro, Enterprise

    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    @Column(nullable = false)
    private int creditsPerMonth;

    @Column(nullable = false)
    private int maxReportsPerMonth;

    @Column(length = 2000)
    private String featuresJson; // JSON array of features

    private boolean isActive = true;

    private int sortOrder;
}

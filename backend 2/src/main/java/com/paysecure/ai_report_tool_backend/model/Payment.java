package com.paysecure.ai_report_tool_backend.model;

import com.paysecure.ai_report_tool_backend.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    private int creditsGranted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String paymentProvider = "paysecure";

    private String externalPaymentId; // Paysecure transaction ID

    private String paymentMethod; // card, bank_transfer, etc.

    @Column(length = 2000)
    private String responseJson; // Raw response from payment provider

    private Instant createdAt;

    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

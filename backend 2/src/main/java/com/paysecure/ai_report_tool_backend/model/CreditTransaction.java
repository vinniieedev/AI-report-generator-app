package com.paysecure.ai_report_tool_backend.model;

import com.paysecure.ai_report_tool_backend.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private int credits; // positive for credit, negative for debit

    private String referenceId; // report_id, payment_id, etc.

    @Column(length = 500)
    private String description;

    private int balanceAfter; // balance after this transaction

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

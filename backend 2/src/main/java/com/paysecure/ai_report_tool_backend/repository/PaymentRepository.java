package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.Payment;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByUserOrderByCreatedAtDesc(User user);
    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
}

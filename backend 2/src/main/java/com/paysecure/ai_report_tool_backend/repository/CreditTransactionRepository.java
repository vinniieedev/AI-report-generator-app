package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.CreditTransaction;
import com.paysecure.ai_report_tool_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    List<CreditTransaction> findByUserOrderByCreatedAtDesc(User user);
    Page<CreditTransaction> findByUser(User user, Pageable pageable);
}

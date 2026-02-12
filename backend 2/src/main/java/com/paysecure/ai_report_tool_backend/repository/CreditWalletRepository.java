package com.paysecure.ai_report_tool_backend.repository;

import com.paysecure.ai_report_tool_backend.model.CreditWallet;
import com.paysecure.ai_report_tool_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CreditWalletRepository extends JpaRepository<CreditWallet, UUID> {
    Optional<CreditWallet> findByUser(User user);
}

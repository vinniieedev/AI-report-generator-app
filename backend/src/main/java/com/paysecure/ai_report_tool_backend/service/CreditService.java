package com.paysecure.ai_report_tool_backend.service;

import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.CreditTransaction;
import com.paysecure.ai_report_tool_backend.model.CreditWallet;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.enums.TransactionType;
import com.paysecure.ai_report_tool_backend.repository.CreditTransactionRepository;
import com.paysecure.ai_report_tool_backend.repository.CreditWalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CreditService {

    private final CreditWalletRepository walletRepository;
    private final CreditTransactionRepository transactionRepository;

    public CreditService(
            CreditWalletRepository walletRepository,
            CreditTransactionRepository transactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public CreditWallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> {
                    CreditWallet wallet = new CreditWallet();
                    wallet.setUser(user);
                    wallet.setBalance(user.getCredits());
                    return walletRepository.save(wallet);
                });
    }

    public int getBalance(User user) {
        return getOrCreateWallet(user).getBalance();
    }

    @Transactional
    public CreditTransaction addCredits(
            User user,
            int credits,
            TransactionType type,
            String referenceId,
            String description
    ) {
        CreditWallet wallet = getOrCreateWallet(user);
        wallet.setBalance(wallet.getBalance() + credits);
        walletRepository.save(wallet);

        // Update user credits for backward compatibility
        user.setCredits(wallet.getBalance());

        CreditTransaction transaction = new CreditTransaction();
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setCredits(credits);
        transaction.setReferenceId(referenceId);
        transaction.setDescription(description);
        transaction.setBalanceAfter(wallet.getBalance());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public CreditTransaction deductCredits(
            User user,
            int credits,
            TransactionType type,
            String referenceId,
            String description
    ) {
        CreditWallet wallet = getOrCreateWallet(user);

        if (wallet.getBalance() < credits) {
            throw new ApiException("Insufficient credits", HttpStatus.BAD_REQUEST);
        }

        wallet.setBalance(wallet.getBalance() - credits);
        walletRepository.save(wallet);

        // Update user credits for backward compatibility
        user.setCredits(wallet.getBalance());

        CreditTransaction transaction = new CreditTransaction();
        transaction.setUser(user);
        transaction.setType(type);
        transaction.setCredits(-credits); // Negative for deduction
        transaction.setReferenceId(referenceId);
        transaction.setDescription(description);
        transaction.setBalanceAfter(wallet.getBalance());

        return transactionRepository.save(transaction);
    }

    public boolean hasEnoughCredits(User user, int required) {
        return getBalance(user) >= required;
    }

    public List<CreditTransaction> getTransactionHistory(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }
}

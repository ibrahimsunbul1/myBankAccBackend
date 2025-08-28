package com.mybankaccount.backend.service;

import com.mybankaccount.backend.entity.Account;
import com.mybankaccount.backend.entity.Transaction;
import com.mybankaccount.backend.repository.TransactionRepository;
import com.mybankaccount.backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountService accountService;
    
    // Transfer money between accounts
    public Transaction transferMoney(String fromAccountNumber, String toAccountNumber, 
                                   BigDecimal amount, String description) {
        // Validate input
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        // Find accounts
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
            .orElseThrow(() -> new RuntimeException("Source account not found: " + fromAccountNumber));
        
        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
            .orElseThrow(() -> new RuntimeException("Destination account not found: " + toAccountNumber));
        
        // Check if accounts are active
        if (!fromAccount.isActive()) {
            throw new RuntimeException("Source account is not active");
        }
        
        if (!toAccount.isActive()) {
            throw new RuntimeException("Destination account is not active");
        }
        
        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source account");
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            // Save transaction first
            transaction = transactionRepository.save(transaction);
            
            // Perform the transfer
            fromAccount.withdraw(amount);
            toAccount.deposit(amount);
            
            // Update accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            // Mark transaction as completed
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            return transaction;
            
        } catch (Exception e) {
            // Mark transaction as failed
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }
    
    // Deposit money to account
    public Transaction depositMoney(String accountNumber, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        if (!account.isActive()) {
            throw new RuntimeException("Account is not active");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setToAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            transaction = transactionRepository.save(transaction);
            
            account.deposit(amount);
            accountRepository.save(account);
            
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Deposit failed: " + e.getMessage(), e);
        }
    }
    
    // Withdraw money from account
    public Transaction withdrawMoney(String accountNumber, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        if (!account.isActive()) {
            throw new RuntimeException("Account is not active");
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(description);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        try {
            transaction = transactionRepository.save(transaction);
            
            account.withdraw(amount);
            accountRepository.save(account);
            
            transaction.markAsCompleted();
            transaction = transactionRepository.save(transaction);
            
            return transaction;
            
        } catch (Exception e) {
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Withdrawal failed: " + e.getMessage(), e);
        }
    }
    
    // Get transaction by reference
    public Optional<Transaction> getTransactionByReference(String transactionReference) {
        return transactionRepository.findByTransactionReference(transactionReference);
    }
    
    // Get transaction by ID
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    
    // Get account transactions
    public Page<Transaction> getAccountTransactions(String accountNumber, Pageable pageable) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        return transactionRepository.findByAccount(account, pageable);
    }
    
    // Get account transactions (list)
    public List<Transaction> getAccountTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        return transactionRepository.findByAccount(account);
    }
    
    // Get transactions by date range
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByDateRange(startDate, endDate);
    }
    
    // Get account transactions by date range
    public List<Transaction> getAccountTransactionsByDateRange(String accountNumber, 
                                                              LocalDateTime startDate, 
                                                              LocalDateTime endDate) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        return transactionRepository.findByAccountAndDateRange(account, startDate, endDate);
    }
    
    // Get transactions by status
    public List<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    // Get recent transactions for account
    public List<Transaction> getRecentAccountTransactions(String accountNumber, int days) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return transactionRepository.findRecentTransactionsByAccount(account, since);
    }
    
    // Cancel pending transaction
    public Transaction cancelTransaction(String transactionReference) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionReference));
        
        if (!transaction.isPending()) {
            throw new RuntimeException("Only pending transactions can be cancelled");
        }
        
        transaction.markAsCancelled();
        return transactionRepository.save(transaction);
    }
    
    // Get account balance summary
    public AccountBalanceSummary getAccountBalanceSummary(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        
        BigDecimal totalIncoming = transactionRepository.sumIncomingAmountByAccount(account);
        BigDecimal totalOutgoing = transactionRepository.sumOutgoingAmountByAccount(account);
        long transactionCount = transactionRepository.countByAccount(account);
        
        return new AccountBalanceSummary(account.getBalance(), totalIncoming, totalOutgoing, transactionCount);
    }
    
    // Generate unique transaction reference
    private String generateTransactionReference() {
        String reference;
        do {
            reference = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (transactionRepository.existsByTransactionReference(reference));
        
        return reference;
    }
    
    // Inner class for balance summary
    public static class AccountBalanceSummary {
        private final BigDecimal currentBalance;
        private final BigDecimal totalIncoming;
        private final BigDecimal totalOutgoing;
        private final long transactionCount;
        
        public AccountBalanceSummary(BigDecimal currentBalance, BigDecimal totalIncoming, 
                                   BigDecimal totalOutgoing, long transactionCount) {
            this.currentBalance = currentBalance;
            this.totalIncoming = totalIncoming != null ? totalIncoming : BigDecimal.ZERO;
            this.totalOutgoing = totalOutgoing != null ? totalOutgoing : BigDecimal.ZERO;
            this.transactionCount = transactionCount;
        }
        
        // Getters
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public BigDecimal getTotalIncoming() { return totalIncoming; }
        public BigDecimal getTotalOutgoing() { return totalOutgoing; }
        public long getTransactionCount() { return transactionCount; }
        public BigDecimal getNetFlow() { return totalIncoming.subtract(totalOutgoing); }
    }
}
package com.mybankaccount.backend.service;

import com.mybankaccount.backend.entity.Account;
import com.mybankaccount.backend.entity.Payment;
import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    
    public PaymentService(PaymentRepository paymentRepository, 
                         AccountService accountService,
                         TransactionService transactionService) {
        this.paymentRepository = paymentRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }
    
    public Payment createPayment(User user, Long accountId, Payment.PaymentType paymentType, 
                               BigDecimal amount, String recipientName, String recipientAccount, 
                               String paymentReference, String description) {
        
        validatePaymentRequest(amount, recipientName);
        
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        validateAccountOwnership(user, account);
        validateSufficientBalance(account, amount);
        
        Payment payment = new Payment(user, account, paymentType, amount, recipientName, description);
        payment.setRecipientAccount(recipientAccount);
        payment.setPaymentReference(paymentReference);
        payment.setTransactionId(generateTransactionId());
        
        return paymentRepository.save(payment);
    }
    
    public Payment processPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }
        
        try {
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            
            // Simulate payment processing
            processPaymentTransaction(payment);
            
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setProcessedAt(LocalDateTime.now());
            
            return paymentRepository.save(payment);
            
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }
    
    public Payment cancelPayment(Long paymentId, User user) {
        Payment payment = getPaymentById(paymentId);
        
        validatePaymentOwnership(user, payment);
        
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }
        
        payment.setStatus(Payment.PaymentStatus.CANCELLED);
        payment.setProcessedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getUserPaymentsByStatus(User user, Payment.PaymentStatus status) {
        return paymentRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getUserPaymentsByType(User user, Payment.PaymentType paymentType) {
        return paymentRepository.findByUserAndPaymentTypeOrderByCreatedAtDesc(user, paymentType);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getUserPaymentsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    @Transactional(readOnly = true)
    public PaymentSummary getPaymentSummary(User user) {
        Long pendingCount = paymentRepository.countByUserAndStatus(user, Payment.PaymentStatus.PENDING);
        Long completedCount = paymentRepository.countByUserAndStatus(user, Payment.PaymentStatus.COMPLETED);
        Long failedCount = paymentRepository.countByUserAndStatus(user, Payment.PaymentStatus.FAILED);
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        
        Double monthlyTotal = paymentRepository.getTotalPaymentAmountByUserAndDateRange(user, startOfMonth, endOfMonth);
        
        return new PaymentSummary(pendingCount, completedCount, failedCount, 
                                BigDecimal.valueOf(monthlyTotal != null ? monthlyTotal : 0.0));
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getRecentPayments(User user) {
        return paymentRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }
    
    private void validatePaymentRequest(BigDecimal amount, String recipientName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new RuntimeException("Payment amount cannot exceed 100,000 TL");
        }
        
        if (recipientName == null || recipientName.trim().isEmpty()) {
            throw new RuntimeException("Recipient name is required");
        }
    }
    
    private void validateAccountOwnership(User user, Account account) {
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Account does not belong to the user");
        }
    }
    
    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance for payment");
        }
    }
    
    private void validatePaymentOwnership(User user, Payment payment) {
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Payment does not belong to the user");
        }
    }
    
    private void processPaymentTransaction(Payment payment) {
        // Create a withdrawal transaction for the payment
        transactionService.withdrawMoney(
            payment.getAccount().getAccountNumber(),
            payment.getAmount(),
            "Payment: " + payment.getPaymentType() + " - " + payment.getRecipientName()
        );
        
        // Account balance is already updated by withdrawMoney
        // No need to call updateAccount separately
    }
    
    private String generateTransactionId() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public static class PaymentSummary {
        private final Long pendingCount;
        private final Long completedCount;
        private final Long failedCount;
        private final BigDecimal monthlyTotal;
        
        public PaymentSummary(Long pendingCount, Long completedCount, Long failedCount, BigDecimal monthlyTotal) {
            this.pendingCount = pendingCount;
            this.completedCount = completedCount;
            this.failedCount = failedCount;
            this.monthlyTotal = monthlyTotal;
        }
        
        public Long getPendingCount() { return pendingCount; }
        public Long getCompletedCount() { return completedCount; }
        public Long getFailedCount() { return failedCount; }
        public BigDecimal getMonthlyTotal() { return monthlyTotal; }
    }
}
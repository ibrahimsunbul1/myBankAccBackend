package com.mybankaccount.backend.dto;

import com.mybankaccount.backend.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private Long id;
    private Long accountId;
    private String accountNumber;
    private Payment.PaymentType paymentType;
    private Payment.PaymentStatus status;
    private BigDecimal amount;
    private String recipientName;
    private String recipientAccount;
    private String paymentReference;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String transactionId;
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.accountId = payment.getAccount().getId();
        this.accountNumber = payment.getAccount().getAccountNumber();
        this.paymentType = payment.getPaymentType();
        this.status = payment.getStatus();
        this.amount = payment.getAmount();
        this.recipientName = payment.getRecipientName();
        this.recipientAccount = payment.getRecipientAccount();
        this.paymentReference = payment.getPaymentReference();
        this.description = payment.getDescription();
        this.createdAt = payment.getCreatedAt();
        this.processedAt = payment.getProcessedAt();
        this.transactionId = payment.getTransactionId();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public Payment.PaymentType getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(Payment.PaymentType paymentType) {
        this.paymentType = paymentType;
    }
    
    public Payment.PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(Payment.PaymentStatus status) {
        this.status = status;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getRecipientName() {
        return recipientName;
    }
    
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
    
    public String getRecipientAccount() {
        return recipientAccount;
    }
    
    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }
    
    public String getPaymentReference() {
        return paymentReference;
    }
    
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
package com.mybankaccount.backend.dto;

import com.mybankaccount.backend.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String transactionReference;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private Transaction.TransactionType transactionType;
    private Transaction.TransactionStatus status;
    private String description;
    private BigDecimal fee;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String currency;
    private String failureReason;
    
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.transactionReference = transaction.getTransactionReference();
        this.fromAccountNumber = transaction.getFromAccount() != null ? 
            transaction.getFromAccount().getAccountNumber() : null;
        this.toAccountNumber = transaction.getToAccount() != null ? 
            transaction.getToAccount().getAccountNumber() : null;
        this.amount = transaction.getAmount();
        this.transactionType = transaction.getTransactionType();
        this.status = transaction.getStatus();
        this.description = transaction.getDescription();
        this.fee = transaction.getFee();
        this.createdAt = transaction.getCreatedAt();
        this.processedAt = transaction.getProcessedAt();
        this.currency = transaction.getCurrency();
        this.failureReason = transaction.getFailureReason();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTransactionReference() {
        return transactionReference;
    }
    
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    
    public String getFromAccountNumber() {
        return fromAccountNumber;
    }
    
    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }
    
    public String getToAccountNumber() {
        return toAccountNumber;
    }
    
    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Transaction.TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(Transaction.TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public Transaction.TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getFee() {
        return fee;
    }
    
    public void setFee(BigDecimal fee) {
        this.fee = fee;
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
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
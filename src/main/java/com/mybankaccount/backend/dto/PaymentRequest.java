package com.mybankaccount.backend.dto;

import com.mybankaccount.backend.entity.Payment;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {
    
    @NotNull(message = "Account ID is required")
    private Long accountId;
    
    @NotNull(message = "Payment type is required")
    private Payment.PaymentType paymentType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "100000.00", message = "Amount cannot exceed 100,000 TL")
    private BigDecimal amount;
    
    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Recipient name must be between 2 and 100 characters")
    private String recipientName;
    
    @Size(max = 50, message = "Recipient account cannot exceed 50 characters")
    private String recipientAccount;
    
    @Size(max = 50, message = "Payment reference cannot exceed 50 characters")
    private String paymentReference;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(Long accountId, Payment.PaymentType paymentType, BigDecimal amount, 
                         String recipientName, String recipientAccount, String paymentReference, 
                         String description) {
        this.accountId = accountId;
        this.paymentType = paymentType;
        this.amount = amount;
        this.recipientName = recipientName;
        this.recipientAccount = recipientAccount;
        this.paymentReference = paymentReference;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public Payment.PaymentType getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(Payment.PaymentType paymentType) {
        this.paymentType = paymentType;
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
}
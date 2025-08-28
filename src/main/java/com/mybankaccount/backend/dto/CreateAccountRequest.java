package com.mybankaccount.backend.dto;

import com.mybankaccount.backend.entity.Account;
import jakarta.validation.constraints.NotNull;

public class CreateAccountRequest {
    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
    
    public Account.AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }
}
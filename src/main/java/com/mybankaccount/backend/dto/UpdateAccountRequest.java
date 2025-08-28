package com.mybankaccount.backend.dto;

import com.mybankaccount.backend.entity.Account;

public class UpdateAccountRequest {
    private Account.AccountType accountType;
    private String currency;
    
    public Account.AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
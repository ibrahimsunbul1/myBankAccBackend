package com.mybankaccount.backend.service;

import com.mybankaccount.backend.entity.Account;
import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.repository.AccountRepository;
import com.mybankaccount.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.secureRandom = new SecureRandom();
    }
    
    // Create new account
    public Account createAccount(Long userId, Account.AccountType accountType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!user.getIsActive()) {
            throw new RuntimeException("Cannot create account for inactive user");
        }
        
        String accountNumber = generateAccountNumber();
        
        Account account = new Account(accountNumber, user, accountType);
        return accountRepository.save(account);
    }
    
    // Get account by ID
    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }
    
    // Get account by account number
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    // Get active account by account number
    @Transactional(readOnly = true)
    public Optional<Account> getActiveAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumberAndIsActiveTrue(accountNumber);
    }
    
    // Get all accounts by user
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }
    
    // Get active accounts by user
    @Transactional(readOnly = true)
    public List<Account> getActiveAccountsByUser(Long userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    // Update account
    public Account updateAccount(Long accountId, Account.AccountType accountType, String currency) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        
        if (accountType != null) {
            account.setAccountType(accountType);
        }
        
        if (currency != null && !currency.trim().isEmpty()) {
            account.setCurrency(currency.toUpperCase());
        }
        
        return accountRepository.save(account);
    }
    
    // Deactivate account
    public void deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Cannot deactivate account with non-zero balance");
        }
        
        account.setIsActive(false);
        accountRepository.save(account);
    }
    
    // Activate account
    public void activateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        
        account.setIsActive(true);
        accountRepository.save(account);
    }
    
    // Deposit money
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }
        
        Account account = getActiveAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Active account not found with number: " + accountNumber));
        
        account.deposit(amount);
        return accountRepository.save(account);
    }
    
    // Withdraw money
    public Account withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }
        
        Account account = getActiveAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Active account not found with number: " + accountNumber));
        
        if (!account.withdraw(amount)) {
            throw new RuntimeException("Insufficient balance for withdrawal");
        }
        
        return accountRepository.save(account);
    }
    
    // Check balance
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        Account account = getActiveAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Active account not found with number: " + accountNumber));
        
        return account.getBalance();
    }
    
    // Check if account belongs to user
    @Transactional(readOnly = true)
    public boolean isAccountOwnedByUser(String accountNumber, Long userId) {
        Optional<Account> account = accountRepository.findByAccountNumberAndUserId(accountNumber, userId);
        return account.isPresent();
    }
    
    // Generate unique account number
    private String generateAccountNumber() {
        String accountNumber;
        do {
            // Generate 12-digit account number
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(secureRandom.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }
    
    // Get account count by user
    @Transactional(readOnly = true)
    public long getAccountCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return accountRepository.countByUser(user);
    }
    
    // Get active account count by user
    @Transactional(readOnly = true)
    public long getActiveAccountCountByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return accountRepository.countByUserAndIsActiveTrue(user);
    }
}
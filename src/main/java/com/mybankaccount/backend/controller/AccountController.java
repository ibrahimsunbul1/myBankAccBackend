package com.mybankaccount.backend.controller;

import com.mybankaccount.backend.dto.BalanceResponse;
import com.mybankaccount.backend.dto.CreateAccountRequest;
import com.mybankaccount.backend.dto.TransactionRequest;
import com.mybankaccount.backend.dto.UpdateAccountRequest;
import com.mybankaccount.backend.entity.Account;
import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountController {
    
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    // Create new account
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Account account = accountService.createAccount(currentUser.getId(), request.getAccountType());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
    
    // Get user's accounts
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserAccounts(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Account> accounts = accountService.getActiveAccountsByUser(currentUser.getId());
        return ResponseEntity.ok(accounts);
    }
    
    // Get account by ID
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId, Authentication authentication) {
        Optional<Account> account = accountService.getAccountById(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = (User) authentication.getPrincipal();
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !account.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        return ResponseEntity.ok(account.get());
    }
    
    // Get account by account number
    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountByNumber(@PathVariable String accountNumber, Authentication authentication) {
        Optional<Account> account = accountService.getActiveAccountByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = (User) authentication.getPrincipal();
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !account.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        return ResponseEntity.ok(account.get());
    }
    
    // Update account
    @PutMapping("/{accountId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, 
                                         @Valid @RequestBody UpdateAccountRequest request, 
                                         Authentication authentication) {
        Optional<Account> existingAccount = accountService.getAccountById(accountId);
        if (existingAccount.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = (User) authentication.getPrincipal();
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !existingAccount.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        Account updatedAccount = accountService.updateAccount(accountId, request.getAccountType(), request.getCurrency());
        return ResponseEntity.ok(updatedAccount);
    }
    
    // Deactivate account
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long accountId, Authentication authentication) {
        Optional<Account> existingAccount = accountService.getAccountById(accountId);
        if (existingAccount.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = (User) authentication.getPrincipal();
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !existingAccount.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        accountService.deactivateAccount(accountId);
        return ResponseEntity.ok("Account deactivated successfully");
    }
    
    // Deposit money
    @PostMapping("/{accountNumber}/deposit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deposit(@PathVariable String accountNumber, 
                                   @Valid @RequestBody TransactionRequest request, 
                                   Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !accountService.isAccountOwnedByUser(accountNumber, currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        Account account = accountService.deposit(accountNumber, request.getAmount());
        return ResponseEntity.ok(account);
    }
    
    // Withdraw money
    @PostMapping("/{accountNumber}/withdraw")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> withdraw(@PathVariable String accountNumber, 
                                    @Valid @RequestBody TransactionRequest request, 
                                    Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !accountService.isAccountOwnedByUser(accountNumber, currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        Account account = accountService.withdraw(accountNumber, request.getAmount());
        return ResponseEntity.ok(account);
    }
    
    // Get balance
    @GetMapping("/{accountNumber}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        // Check if account belongs to current user (unless admin)
        if (!currentUser.getRole().equals(User.Role.ADMIN) && 
            !accountService.isAccountOwnedByUser(accountNumber, currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(new BalanceResponse(balance));
    }
    

}
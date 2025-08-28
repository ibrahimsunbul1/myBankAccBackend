package com.mybankaccount.backend.controller;

import com.mybankaccount.backend.dto.DepositRequest;
import com.mybankaccount.backend.dto.TransactionResponse;
import com.mybankaccount.backend.dto.TransferRequest;
import com.mybankaccount.backend.dto.WithdrawRequest;
import com.mybankaccount.backend.entity.Transaction;
import com.mybankaccount.backend.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    // Transfer money between accounts
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> transferMoney(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.transferMoney(
            request.getFromAccountNumber(),
            request.getToAccountNumber(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }
    
    // Deposit money to account
    @PostMapping("/deposit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> depositMoney(@Valid @RequestBody DepositRequest request) {
        Transaction transaction = transactionService.depositMoney(
            request.getAccountNumber(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }
    
    // Withdraw money from account
    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> withdrawMoney(@Valid @RequestBody WithdrawRequest request) {
        Transaction transaction = transactionService.withdrawMoney(
            request.getAccountNumber(),
            request.getAmount(),
            request.getDescription()
        );
        
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }
    
    // Get transaction by reference
    @GetMapping("/reference/{transactionReference}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionByReference(@PathVariable String transactionReference) {
        return transactionService.getTransactionByReference(transactionReference)
            .map(transaction -> ResponseEntity.ok(new TransactionResponse(transaction)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Get transaction by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
            .map(transaction -> ResponseEntity.ok(new TransactionResponse(transaction)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Get account transactions (paginated)
    @GetMapping("/account/{accountNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getAccountTransactions(accountNumber, pageable);
        
        Page<TransactionResponse> response = transactions.map(TransactionResponse::new);
        return ResponseEntity.ok(response);
    }
    
    // Get account transactions (all)
    @GetMapping("/account/{accountNumber}/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllAccountTransactions(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber);
        List<TransactionResponse> response = transactions.stream()
            .map(TransactionResponse::new)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Get recent account transactions
    @GetMapping("/account/{accountNumber}/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRecentAccountTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "30") int days) {
        List<Transaction> transactions = transactionService.getRecentAccountTransactions(accountNumber, days);
        List<TransactionResponse> response = transactions.stream()
            .map(TransactionResponse::new)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Get transactions by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        List<TransactionResponse> response = transactions.stream()
            .map(TransactionResponse::new)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Get account transactions by date range
    @GetMapping("/account/{accountNumber}/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountTransactionsByDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Transaction> transactions = transactionService.getAccountTransactionsByDateRange(
            accountNumber, startDate, endDate);
        List<TransactionResponse> response = transactions.stream()
            .map(TransactionResponse::new)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Get transactions by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionsByStatus(@PathVariable Transaction.TransactionStatus status) {
        List<Transaction> transactions = transactionService.getTransactionsByStatus(status);
        List<TransactionResponse> response = transactions.stream()
            .map(TransactionResponse::new)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Cancel transaction
    @PutMapping("/cancel/{transactionReference}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelTransaction(@PathVariable String transactionReference) {
        Transaction transaction = transactionService.cancelTransaction(transactionReference);
        return ResponseEntity.ok(new TransactionResponse(transaction));
    }
    
    // Get account balance summary
    @GetMapping("/account/{accountNumber}/summary")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAccountBalanceSummary(@PathVariable String accountNumber) {
        TransactionService.AccountBalanceSummary summary = 
            transactionService.getAccountBalanceSummary(accountNumber);
        return ResponseEntity.ok(summary);
    }
    
}
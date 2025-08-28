package com.mybankaccount.backend.repository;

import com.mybankaccount.backend.entity.Transaction;
import com.mybankaccount.backend.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find by transaction reference
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    // Find transactions by account (either from or to)
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccount(@Param("account") Account account, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    List<Transaction> findByAccount(@Param("account") Account account);
    
    // Find transactions by from account
    Page<Transaction> findByFromAccountOrderByCreatedAtDesc(Account fromAccount, Pageable pageable);
    List<Transaction> findByFromAccountOrderByCreatedAtDesc(Account fromAccount);
    
    // Find transactions by to account
    Page<Transaction> findByToAccountOrderByCreatedAtDesc(Account toAccount, Pageable pageable);
    List<Transaction> findByToAccountOrderByCreatedAtDesc(Account toAccount);
    
    // Find by status
    List<Transaction> findByStatusOrderByCreatedAtDesc(Transaction.TransactionStatus status);
    Page<Transaction> findByStatusOrderByCreatedAtDesc(Transaction.TransactionStatus status, Pageable pageable);
    
    // Find by transaction type
    List<Transaction> findByTransactionTypeOrderByCreatedAtDesc(Transaction.TransactionType transactionType);
    Page<Transaction> findByTransactionTypeOrderByCreatedAtDesc(Transaction.TransactionType transactionType, Pageable pageable);
    
    // Find by date range
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);
    
    // Find account transactions by date range
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountAndDateRange(@Param("account") Account account,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountAndDateRange(@Param("account") Account account,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);
    
    // Find by amount range
    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.createdAt DESC")
    List<Transaction> findByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                       @Param("maxAmount") BigDecimal maxAmount);
    
    // Count transactions by status
    long countByStatus(Transaction.TransactionStatus status);
    
    // Count transactions by account
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account")
    long countByAccount(@Param("account") Account account);
    
    // Sum of amounts by account (outgoing)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccount = :account AND t.status = 'COMPLETED'")
    BigDecimal sumOutgoingAmountByAccount(@Param("account") Account account);
    
    // Sum of amounts by account (incoming)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccount = :account AND t.status = 'COMPLETED'")
    BigDecimal sumIncomingAmountByAccount(@Param("account") Account account);
    
    // Find pending transactions older than specified time
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' AND t.createdAt < :cutoffTime")
    List<Transaction> findPendingTransactionsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find recent transactions for an account
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "AND t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByAccount(@Param("account") Account account, 
                                                     @Param("since") LocalDateTime since);
    
    // Check if transaction reference exists
    boolean existsByTransactionReference(String transactionReference);
    
    // Find failed transactions with retry count less than max
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.createdAt >= :since")
    List<Transaction> findFailedTransactionsSince(@Param("since") LocalDateTime since);
}
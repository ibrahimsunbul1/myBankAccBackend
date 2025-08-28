package com.mybankaccount.backend.repository;

import com.mybankaccount.backend.entity.Account;
import com.mybankaccount.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // Find account by account number
    Optional<Account> findByAccountNumber(String accountNumber);
    
    // Find all accounts by user
    List<Account> findByUser(User user);
    
    // Find all accounts by user ID
    List<Account> findByUserId(Long userId);
    
    // Find active accounts by user
    List<Account> findByUserAndIsActiveTrue(User user);
    
    // Find active accounts by user ID
    List<Account> findByUserIdAndIsActiveTrue(Long userId);
    
    // Check if account number exists
    boolean existsByAccountNumber(String accountNumber);
    
    // Find account by account number and user
    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);
    
    // Find account by account number and user ID
    Optional<Account> findByAccountNumberAndUserId(String accountNumber, Long userId);
    
    // Find active account by account number
    Optional<Account> findByAccountNumberAndIsActiveTrue(String accountNumber);
    
    // Count accounts by user
    long countByUser(User user);
    
    // Count active accounts by user
    long countByUserAndIsActiveTrue(User user);
    
    // Find accounts by account type
    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.isActive = true")
    List<Account> findByAccountTypeAndActive(@Param("accountType") Account.AccountType accountType);
    
    // Find user's accounts by account type
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType AND a.isActive = true")
    List<Account> findByUserIdAndAccountTypeAndActive(@Param("userId") Long userId, @Param("accountType") Account.AccountType accountType);
}
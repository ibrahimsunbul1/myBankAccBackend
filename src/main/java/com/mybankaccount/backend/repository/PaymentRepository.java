package com.mybankaccount.backend.repository;

import com.mybankaccount.backend.entity.Payment;
import com.mybankaccount.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUserOrderByCreatedAtDesc(User user);
    
    List<Payment> findByUserAndStatusOrderByCreatedAtDesc(User user, Payment.PaymentStatus status);
    
    List<Payment> findByUserAndPaymentTypeOrderByCreatedAtDesc(User user, Payment.PaymentType paymentType);
    
    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Payment> findByUserAndDateRange(@Param("user") User user, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user = :user AND p.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user = :user AND p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalPaymentAmountByUserAndDateRange(@Param("user") User user, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    List<Payment> findTop10ByUserOrderByCreatedAtDesc(User user);
}
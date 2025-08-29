package com.mybankaccount.backend.controller;

import com.mybankaccount.backend.dto.PaymentRequest;
import com.mybankaccount.backend.dto.PaymentResponse;
import com.mybankaccount.backend.entity.Payment;
import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request,
                                                       Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        Payment payment = paymentService.createPayment(
            currentUser,
            request.getAccountId(),
            request.getPaymentType(),
            request.getAmount(),
            request.getRecipientName(),
            request.getRecipientAccount(),
            request.getPaymentReference(),
            request.getDescription()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PaymentResponse(payment));
    }
    
    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable Long paymentId,
                                                        Authentication authentication) {
        Payment payment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(new PaymentResponse(payment));
    }
    
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId,
                                                       Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Payment payment = paymentService.cancelPayment(paymentId, currentUser);
        return ResponseEntity.ok(new PaymentResponse(payment));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Payment> payments = paymentService.getUserPayments(currentUser);
        
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentResponses);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPaymentsByStatus(
            @PathVariable Payment.PaymentStatus status,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Payment> payments = paymentService.getUserPaymentsByStatus(currentUser, status);
        
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentResponses);
    }
    
    @GetMapping("/type/{paymentType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPaymentsByType(
            @PathVariable Payment.PaymentType paymentType,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Payment> payments = paymentService.getUserPaymentsByType(currentUser, paymentType);
        
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentResponses);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getUserPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Payment> payments = paymentService.getUserPaymentsByDateRange(currentUser, startDate, endDate);
        
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentResponses);
    }
    
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId,
                                                        Authentication authentication) {
        Payment payment = paymentService.getPaymentById(paymentId);
        User currentUser = (User) authentication.getPrincipal();
        
        // Ensure user can only access their own payments
        if (!payment.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(new PaymentResponse(payment));
    }
    
    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentService.PaymentSummary> getPaymentSummary(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        PaymentService.PaymentSummary summary = paymentService.getPaymentSummary(currentUser);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getRecentPayments(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Payment> payments = paymentService.getRecentPayments(currentUser);
        
        List<PaymentResponse> paymentResponses = payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentResponses);
    }
    
    @GetMapping("/types")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Payment.PaymentType[]> getPaymentTypes() {
        return ResponseEntity.ok(Payment.PaymentType.values());
    }
    
    @GetMapping("/statuses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Payment.PaymentStatus[]> getPaymentStatuses() {
        return ResponseEntity.ok(Payment.PaymentStatus.values());
    }
}
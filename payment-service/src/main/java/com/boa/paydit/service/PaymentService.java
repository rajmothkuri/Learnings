package com.boa.paydit.service;

import com.boa.paydit.dto.PaymentDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Payment;
import com.boa.paydit.entity.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface PaymentService {
    CompletableFuture<PaymentDTO> createPayment(PaymentDTO paymentDTO);
    CompletableFuture<PaymentDTO> updatePayment(Long id, PaymentDTO paymentDTO);
    CompletableFuture<PaymentDTO> getPayment(Long id);
    CompletableFuture<PageResponse<PaymentDTO>> getAllPayments(Pageable pageable);
    CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByCustomer(Long customerId, Pageable pageable);
    CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByStatus(PaymentStatus status, Pageable pageable);
    CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    CompletableFuture<Void> deletePayment(Long id);
    CompletableFuture<BigDecimal> getTotalAmountPaidByCustomer(Long customerId);
}

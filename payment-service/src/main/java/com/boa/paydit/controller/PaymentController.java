package com.boa.paydit.controller;

import com.boa.paydit.dto.PaymentDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.PaymentStatus;
import com.boa.paydit.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Payment Controller with pagination, sorting, and filtering
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<PaymentDTO>> createPayment(@RequestBody PaymentDTO paymentDTO) {
        log.info("Creating new payment");
        return paymentService.createPayment(paymentDTO)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<PaymentDTO>> getPayment(@PathVariable Long id) {
        log.info("Getting payment with id: {}", id);
        return paymentService.getPayment(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<PageResponse<PaymentDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting all payments with page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return paymentService.getAllPayments(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<ResponseEntity<PageResponse<PaymentDTO>>> getPaymentsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting payments for customer: {}", customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return paymentService.getPaymentsByCustomer(customerId, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/status/{status}")
    public CompletableFuture<ResponseEntity<PageResponse<PaymentDTO>>> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting payments with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return paymentService.getPaymentsByStatus(status, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/date-range")
    public CompletableFuture<ResponseEntity<PageResponse<PaymentDTO>>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting payments between {} and {}", startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return paymentService.getPaymentsByDateRange(startDate, endDate, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/amount-range")
    public CompletableFuture<ResponseEntity<PageResponse<PaymentDTO>>> getPaymentsByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "amount") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting payments between {} and {}", minAmount, maxAmount);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return paymentService.getPaymentsByAmountRange(minAmount, maxAmount, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<PaymentDTO>> updatePayment(
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO) {
        log.info("Updating payment with id: {}", id);
        return paymentService.updatePayment(id, paymentDTO)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deletePayment(@PathVariable Long id) {
        log.info("Deleting payment with id: {}", id);
        return paymentService.deletePayment(id)
                .thenApply(v -> ResponseEntity.noContent().<Void>build());
    }

    @GetMapping("/customer/{customerId}/total-paid")
    public CompletableFuture<ResponseEntity<BigDecimal>> getTotalPaid(@PathVariable Long customerId) {
        log.info("Getting total amount paid by customer: {}", customerId);
        return paymentService.getTotalAmountPaidByCustomer(customerId)
                .thenApply(ResponseEntity::ok);
    }
}

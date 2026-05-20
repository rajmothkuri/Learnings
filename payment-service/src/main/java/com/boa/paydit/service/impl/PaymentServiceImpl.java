package com.boa.paydit.service.impl;

import com.boa.paydit.dto.PaymentDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Customer;
import com.boa.paydit.entity.Invoice;
import com.boa.paydit.entity.Payment;
import com.boa.paydit.entity.PaymentStatus;
import com.boa.paydit.exception.PaymentException;
import com.boa.paydit.exception.ResourceNotFoundException;
import com.boa.paydit.exception.ValidationException;
import com.boa.paydit.repository.CustomerRepository;
import com.boa.paydit.repository.InvoiceRepository;
import com.boa.paydit.repository.PaymentRepository;
import com.boa.paydit.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Payment Service Implementation with CompletableFuture and thread concurrency
 */
@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExecutorService executorService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                            CustomerRepository customerRepository,
                            InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        // Virtual threads for better concurrency (Java 19+)
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<PaymentDTO> createPayment(PaymentDTO paymentDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating payment for customer: {}", paymentDTO.getCustomerId());

            // Validation
            if (paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("INVALID_AMOUNT", "Payment amount must be greater than zero");
            }

            // Fetch customer and invoice in parallel
            var customerFuture = CompletableFuture.supplyAsync(() ->
                    customerRepository.findById(paymentDTO.getCustomerId())
                            .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                                    "Customer not found with id: " + paymentDTO.getCustomerId())), executorService);

            var invoiceFuture = CompletableFuture.supplyAsync(() ->
                    invoiceRepository.findById(paymentDTO.getInvoiceId())
                            .orElseThrow(() -> new ResourceNotFoundException("INVOICE_NOT_FOUND",
                                    "Invoice not found with id: " + paymentDTO.getInvoiceId())), executorService);

            // Wait for both futures
            Customer customer = customerFuture.join();
            Invoice invoice = invoiceFuture.join();

            // Create payment
            Payment payment = new Payment();
            payment.setAmount(paymentDTO.getAmount());
            payment.setDescription(paymentDTO.getDescription());
            payment.setPaymentMethod(paymentDTO.getPaymentMethod());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setCustomer(customer);
            payment.setInvoice(invoice);

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with id: {}", savedPayment.getId());

            return convertToDTO(savedPayment);
        }, executorService).exceptionally(ex -> {
            log.error("Error creating payment", ex);
            throw new PaymentException("CREATE_PAYMENT_FAILED", "Failed to create payment: " + ex.getMessage(), ex);
        });
    }

    @Override
    public CompletableFuture<PaymentDTO> updatePayment(Long id, PaymentDTO paymentDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Updating payment with id: {}", id);

            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND",
                            "Payment not found with id: " + id));

            if (paymentDTO.getAmount() != null && paymentDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                payment.setAmount(paymentDTO.getAmount());
            }

            if (paymentDTO.getDescription() != null) {
                payment.setDescription(paymentDTO.getDescription());
            }

            if (paymentDTO.getPaymentMethod() != null) {
                payment.setPaymentMethod(paymentDTO.getPaymentMethod());
            }

            Payment updatedPayment = paymentRepository.save(payment);
            log.info("Payment updated with id: {}", id);

            return convertToDTO(updatedPayment);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PaymentDTO> getPayment(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching payment with id: {}", id);
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND",
                            "Payment not found with id: " + id));
            return convertToDTO(payment);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<PaymentDTO>> getAllPayments(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching all payments with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Payment> payments = paymentRepository.findAll(pageable);
            return PageResponse.fromPage(payments.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching payments for customer: {}", customerId);
            // Verify customer exists
            customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                            "Customer not found with id: " + customerId));

            Page<Payment> payments = paymentRepository.findByCustomerId(customerId, pageable);
            return PageResponse.fromPage(payments.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching payments with status: {}", status);
            Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
            return PageResponse.fromPage(payments.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching payments between {} and {}", startDate, endDate);
            Page<Payment> payments = paymentRepository.findPaymentsByDateRange(startDate, endDate, pageable);
            return PageResponse.fromPage(payments.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<PaymentDTO>> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching payments between {} and {}", minAmount, maxAmount);
            Page<Payment> payments = paymentRepository.findPaymentsByAmountRange(minAmount, maxAmount, pageable);
            return PageResponse.fromPage(payments.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> deletePayment(Long id) {
        return CompletableFuture.runAsync(() -> {
            log.info("Deleting payment with id: {}", id);
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND",
                            "Payment not found with id: " + id));
            paymentRepository.delete(payment);
            log.info("Payment deleted with id: {}", id);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<BigDecimal> getTotalAmountPaidByCustomer(Long customerId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Calculating total amount paid by customer: {}", customerId);
            return paymentRepository.getTotalAmountPaidByCustomer(customerId);
        }, executorService);
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus().toString());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setDescription(payment.getDescription());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setCustomerId(payment.getCustomer().getId());
        dto.setInvoiceId(payment.getInvoice().getId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}

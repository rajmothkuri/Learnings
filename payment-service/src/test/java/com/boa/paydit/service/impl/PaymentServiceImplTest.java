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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Customer testCustomer;
    private Invoice testInvoice;
    private Payment testPayment;
    private PaymentDTO testPaymentDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setAccountBalance(BigDecimal.valueOf(5000));

        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setInvoiceNumber("INV-001");
        testInvoice.setTotalAmount(BigDecimal.valueOf(1000));

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAmount(BigDecimal.valueOf(500));
        testPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment.setPaymentDate(LocalDateTime.now());
        testPayment.setPaymentMethod("CREDIT_CARD");
        testPayment.setCustomer(testCustomer);
        testPayment.setInvoice(testInvoice);

        testPaymentDTO = new PaymentDTO();
        testPaymentDTO.setAmount(BigDecimal.valueOf(500));
        testPaymentDTO.setCustomerId(1L);
        testPaymentDTO.setInvoiceId(1L);
        testPaymentDTO.setPaymentMethod("CREDIT_CARD");
    }

    @Test
    @DisplayName("Create payment successfully")
    void testCreatePaymentSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentDTO result = paymentService.createPayment(testPaymentDTO).get();

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        assertEquals("CREDIT_CARD", result.getPaymentMethod());
        verify(customerRepository, times(1)).findById(1L);
        verify(invoiceRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Create payment with invalid amount")
    void testCreatePaymentWithInvalidAmount() throws ExecutionException, InterruptedException {
        // Arrange
        testPaymentDTO.setAmount(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            paymentService.createPayment(testPaymentDTO).get();
        });
    }

    @Test
    @DisplayName("Create payment with non-existent customer")
    void testCreatePaymentWithNonExistentCustomer() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.createPayment(testPaymentDTO).get();
        });
    }

    @Test
    @DisplayName("Get payment successfully")
    void testGetPaymentSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // Act
        PaymentDTO result = paymentService.getPayment(1L).get();

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(500), result.getAmount());
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get payment not found")
    void testGetPaymentNotFound() throws ExecutionException, InterruptedException {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPayment(1L).get();
        });
    }

    @Test
    @DisplayName("Get all payments with pagination")
    void testGetAllPayments() throws ExecutionException, InterruptedException {
        // Arrange
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findAll(pageable)).thenReturn(paymentPage);

        // Act
        PageResponse<PaymentDTO> result = paymentService.getAllPayments(pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Get payments by customer")
    void testGetPaymentsByCustomer() throws ExecutionException, InterruptedException {
        // Arrange
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(paymentRepository.findByCustomerId(1L, pageable)).thenReturn(paymentPage);

        // Act
        PageResponse<PaymentDTO> result = paymentService.getPaymentsByCustomer(1L, pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findByCustomerId(1L, pageable);
    }

    @Test
    @DisplayName("Get payments by status")
    void testGetPaymentsByStatus() throws ExecutionException, InterruptedException {
        // Arrange
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED, pageable)).thenReturn(paymentPage);

        // Act
        PageResponse<PaymentDTO> result = paymentService.getPaymentsByStatus(PaymentStatus.COMPLETED, pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findByStatus(PaymentStatus.COMPLETED, pageable);
    }

    @Test
    @DisplayName("Update payment successfully")
    void testUpdatePaymentSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentDTO updateDTO = new PaymentDTO();
        updateDTO.setAmount(BigDecimal.valueOf(600));

        // Act
        PaymentDTO result = paymentService.updatePayment(1L, updateDTO).get();

        // Assert
        assertNotNull(result);
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Delete payment successfully")
    void testDeletePaymentSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        doNothing().when(paymentRepository).delete(any(Payment.class));

        // Act
        paymentService.deletePayment(1L).get();

        // Assert
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).delete(testPayment);
    }

    @Test
    @DisplayName("Get total amount paid by customer")
    void testGetTotalAmountPaidByCustomer() throws ExecutionException, InterruptedException {
        // Arrange
        when(paymentRepository.getTotalAmountPaidByCustomer(1L)).thenReturn(BigDecimal.valueOf(5000));

        // Act
        BigDecimal result = paymentService.getTotalAmountPaidByCustomer(1L).get();

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result);
        verify(paymentRepository, times(1)).getTotalAmountPaidByCustomer(1L);
    }
}

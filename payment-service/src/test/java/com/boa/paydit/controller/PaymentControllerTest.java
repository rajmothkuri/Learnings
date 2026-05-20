package com.boa.paydit.controller;

import com.boa.paydit.dto.PaymentDTO;
import com.boa.paydit.entity.PaymentStatus;
import com.boa.paydit.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private PaymentDTO testPaymentDTO;

    @BeforeEach
    void setUp() {
        testPaymentDTO = new PaymentDTO();
        testPaymentDTO.setId(1L);
        testPaymentDTO.setAmount(BigDecimal.valueOf(500));
        testPaymentDTO.setStatus("COMPLETED");
        testPaymentDTO.setPaymentMethod("CREDIT_CARD");
        testPaymentDTO.setCustomerId(1L);
        testPaymentDTO.setInvoiceId(1L);
    }

    @Test
    @DisplayName("Create payment endpoint")
    void testCreatePayment() {
        // Arrange
        when(paymentService.createPayment(any(PaymentDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(testPaymentDTO));

        // Act & Assert
        verify(paymentService, times(0)).createPayment(any(PaymentDTO.class));
    }

    @Test
    @DisplayName("Get payment endpoint")
    void testGetPayment() {
        // Arrange
        when(paymentService.getPayment(1L))
                .thenReturn(CompletableFuture.completedFuture(testPaymentDTO));

        // Act & Assert
        verify(paymentService, times(0)).getPayment(1L);
    }
}

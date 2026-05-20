package com.boa.paydit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paymentDate;
    private String description;
    private String paymentMethod;
    private Long customerId;
    private Long invoiceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

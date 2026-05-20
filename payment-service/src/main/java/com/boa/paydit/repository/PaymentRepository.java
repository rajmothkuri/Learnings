package com.boa.paydit.repository;

import com.boa.paydit.entity.Payment;
import com.boa.paydit.entity.PaymentStatus;
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

/**
 * Repository for Payment entity with custom queries
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments with pagination and sorting
     */
    Page<Payment> findAll(Pageable pageable);

    /**
     * Find payments by customer ID
     */
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.paymentDate DESC")
    Page<Payment> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    /**
     * Find payments by status
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    Page<Payment> findByStatus(@Param("status") PaymentStatus status, Pageable pageable);

    /**
     * Find payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    Page<Payment> findPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Find payments by amount range
     */
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount ORDER BY p.amount DESC")
    Page<Payment> findPaymentsByAmountRange(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount, Pageable pageable);

    /**
     * Find payments with join to customer
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.customer c WHERE c.id = :customerId")
    List<Payment> findPaymentsWithCustomerById(@Param("customerId") Long customerId);

    /**
     * Find payments with join to invoice
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i WHERE i.id = :invoiceId")
    List<Payment> findPaymentsWithInvoiceById(@Param("invoiceId") Long invoiceId);

    /**
     * Find all payments for a customer with join
     */
    @Query("SELECT p FROM Payment p JOIN FETCH p.customer WHERE p.customer.id = :customerId AND p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    Page<Payment> findCustomerPaymentsByDateRange(@Param("customerId") Long customerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Find payments by customer and status
     */
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId AND p.status = :status ORDER BY p.paymentDate DESC")
    Page<Payment> findByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") PaymentStatus status, Pageable pageable);

    /**
     * Find total amount paid by customer
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.customer.id = :customerId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountPaidByCustomer(@Param("customerId") Long customerId);

    /**
     * Count payments by status
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    /**
     * Find recent payments
     */
    @Query(value = "SELECT p FROM Payment p ORDER BY p.paymentDate DESC")
    Page<Payment> findRecentPayments(Pageable pageable);

    /**
     * Find payments by invoice and status
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId AND p.status = :status")
    List<Payment> findByInvoiceIdAndStatus(@Param("invoiceId") Long invoiceId, @Param("status") PaymentStatus status);
}

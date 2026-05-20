package com.boa.paydit.repository;

import com.boa.paydit.entity.Invoice;
import com.boa.paydit.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for Invoice entity
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find invoices by status
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = :status ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByStatus(@Param("status") InvoiceStatus status, Pageable pageable);

    /**
     * Find invoices by date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    Page<Invoice> findInvoicesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * Find invoices with payments
     */
    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.payments WHERE i.status = :status ORDER BY i.invoiceDate DESC")
    Page<Invoice> findByStatusWithPayments(@Param("status") InvoiceStatus status, Pageable pageable);

    /**
     * Find overdue invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status != 'FULLY_PAID' AND i.status != 'CANCELLED' ORDER BY i.dueDate ASC")
    Page<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate, Pageable pageable);
}

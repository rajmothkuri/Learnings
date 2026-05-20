package com.boa.paydit.service;

import com.boa.paydit.dto.InvoiceDTO;
import com.boa.paydit.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public interface InvoiceService {
    CompletableFuture<InvoiceDTO> createInvoice(InvoiceDTO invoiceDTO);
    CompletableFuture<InvoiceDTO> updateInvoice(Long id, InvoiceDTO invoiceDTO);
    CompletableFuture<InvoiceDTO> getInvoice(Long id);
    CompletableFuture<PageResponse<InvoiceDTO>> getAllInvoices(Pageable pageable);
    CompletableFuture<PageResponse<InvoiceDTO>> getInvoicesByStatus(String status, Pageable pageable);
    CompletableFuture<PageResponse<InvoiceDTO>> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    CompletableFuture<PageResponse<InvoiceDTO>> getOverdueInvoices(Pageable pageable);
    CompletableFuture<Void> deleteInvoice(Long id);
}

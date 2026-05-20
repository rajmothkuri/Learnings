package com.boa.paydit.controller;

import com.boa.paydit.dto.InvoiceDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Invoice Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<InvoiceDTO>> createInvoice(@RequestBody InvoiceDTO invoiceDTO) {
        log.info("Creating new invoice");
        return invoiceService.createInvoice(invoiceDTO)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<InvoiceDTO>> getInvoice(@PathVariable Long id) {
        log.info("Getting invoice with id: {}", id);
        return invoiceService.getInvoice(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<PageResponse<InvoiceDTO>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting all invoices");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return invoiceService.getAllInvoices(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/status/{status}")
    public CompletableFuture<ResponseEntity<PageResponse<InvoiceDTO>>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting invoices with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return invoiceService.getInvoicesByStatus(status, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/date-range")
    public CompletableFuture<ResponseEntity<PageResponse<InvoiceDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting invoices between {} and {}", startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return invoiceService.getInvoicesByDateRange(startDate, endDate, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/overdue")
    public CompletableFuture<ResponseEntity<PageResponse<InvoiceDTO>>> getOverdue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        log.info("Getting overdue invoices");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return invoiceService.getOverdueInvoices(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<InvoiceDTO>> updateInvoice(
            @PathVariable Long id,
            @RequestBody InvoiceDTO invoiceDTO) {
        log.info("Updating invoice with id: {}", id);
        return invoiceService.updateInvoice(id, invoiceDTO)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteInvoice(@PathVariable Long id) {
        log.info("Deleting invoice with id: {}", id);
        return invoiceService.deleteInvoice(id)
                .thenApply(v -> ResponseEntity.noContent().<Void>build());
    }
}

package com.boa.paydit.service.impl;

import com.boa.paydit.dto.InvoiceDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Invoice;
import com.boa.paydit.entity.InvoiceStatus;
import com.boa.paydit.exception.ResourceNotFoundException;
import com.boa.paydit.exception.ValidationException;
import com.boa.paydit.repository.InvoiceRepository;
import com.boa.paydit.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Invoice Service Implementation with CompletableFuture
 */
@Slf4j
@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ExecutorService executorService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<InvoiceDTO> createInvoice(InvoiceDTO invoiceDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating invoice with number: {}", invoiceDTO.getInvoiceNumber());

            // Validation
            if (invoiceDTO.getInvoiceNumber() == null || invoiceDTO.getInvoiceNumber().isEmpty()) {
                throw new ValidationException("INVALID_INVOICE_NUMBER", "Invoice number cannot be empty");
            }

            if (invoiceRepository.findByInvoiceNumber(invoiceDTO.getInvoiceNumber()).isPresent()) {
                throw new ValidationException("DUPLICATE_INVOICE_NUMBER", "Invoice with this number already exists");
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(invoiceDTO.getInvoiceNumber());
            invoice.setTotalAmount(invoiceDTO.getTotalAmount());
            invoice.setPaidAmount(invoiceDTO.getPaidAmount());
            invoice.setInvoiceDate(invoiceDTO.getInvoiceDate());
            invoice.setDueDate(invoiceDTO.getDueDate());
            invoice.setStatus(InvoiceStatus.DRAFT);
            invoice.setNotes(invoiceDTO.getNotes());

            Invoice savedInvoice = invoiceRepository.save(invoice);
            log.info("Invoice created with id: {}", savedInvoice.getId());

            return convertToDTO(savedInvoice);
        }, executorService);
    }

    @Override
    public CompletableFuture<InvoiceDTO> updateInvoice(Long id, InvoiceDTO invoiceDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Updating invoice with id: {}", id);

            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("INVOICE_NOT_FOUND",
                            "Invoice not found with id: " + id));

            if (invoiceDTO.getPaidAmount() != null) {
                invoice.setPaidAmount(invoiceDTO.getPaidAmount());
            }

            if (invoiceDTO.getStatus() != null) {
                invoice.setStatus(InvoiceStatus.valueOf(invoiceDTO.getStatus()));
            }

            if (invoiceDTO.getNotes() != null) {
                invoice.setNotes(invoiceDTO.getNotes());
            }

            Invoice updatedInvoice = invoiceRepository.save(invoice);
            log.info("Invoice updated with id: {}", id);

            return convertToDTO(updatedInvoice);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<InvoiceDTO> getInvoice(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching invoice with id: {}", id);
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("INVOICE_NOT_FOUND",
                            "Invoice not found with id: " + id));
            return convertToDTO(invoice);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<InvoiceDTO>> getAllInvoices(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching all invoices with pagination");
            Page<Invoice> invoices = invoiceRepository.findAll(pageable);
            return PageResponse.fromPage(invoices.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<InvoiceDTO>> getInvoicesByStatus(String status, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching invoices with status: {}", status);
            InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status);
            Page<Invoice> invoices = invoiceRepository.findByStatus(invoiceStatus, pageable);
            return PageResponse.fromPage(invoices.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<InvoiceDTO>> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching invoices between {} and {}", startDate, endDate);
            Page<Invoice> invoices = invoiceRepository.findInvoicesByDateRange(startDate, endDate, pageable);
            return PageResponse.fromPage(invoices.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<InvoiceDTO>> getOverdueInvoices(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching overdue invoices");
            Page<Invoice> invoices = invoiceRepository.findOverdueInvoices(LocalDate.now(), pageable);
            return PageResponse.fromPage(invoices.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> deleteInvoice(Long id) {
        return CompletableFuture.runAsync(() -> {
            log.info("Deleting invoice with id: {}", id);
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("INVOICE_NOT_FOUND",
                            "Invoice not found with id: " + id));
            invoiceRepository.delete(invoice);
            log.info("Invoice deleted with id: {}", id);
        }, executorService);
    }

    private InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus().toString());
        dto.setNotes(invoice.getNotes());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());
        return dto;
    }
}

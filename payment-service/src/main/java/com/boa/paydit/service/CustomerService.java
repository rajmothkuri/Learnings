package com.boa.paydit.service;

import com.boa.paydit.dto.CustomerDTO;
import com.boa.paydit.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface CustomerService {
    CompletableFuture<CustomerDTO> createCustomer(CustomerDTO customerDTO);
    CompletableFuture<CustomerDTO> updateCustomer(Long id, CustomerDTO customerDTO);
    CompletableFuture<CustomerDTO> getCustomer(Long id);
    CompletableFuture<PageResponse<CustomerDTO>> getAllCustomers(Pageable pageable);
    CompletableFuture<PageResponse<CustomerDTO>> searchCustomersByName(String name, Pageable pageable);
    CompletableFuture<Void> deleteCustomer(Long id);
}

package com.boa.paydit.controller;

import com.boa.paydit.dto.CustomerDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * Customer Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<CustomerDTO>> createCustomer(@RequestBody CustomerDTO customerDTO) {
        log.info("Creating new customer");
        return customerService.createCustomer(customerDTO)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<CustomerDTO>> getCustomer(@PathVariable Long id) {
        log.info("Getting customer with id: {}", id);
        return customerService.getCustomer(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<PageResponse<CustomerDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        log.info("Getting all customers");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return customerService.getAllCustomers(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<PageResponse<CustomerDTO>>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        log.info("Searching customers by name: {}", name);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return customerService.searchCustomersByName(name, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<CustomerDTO>> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDTO customerDTO) {
        log.info("Updating customer with id: {}", id);
        return customerService.updateCustomer(id, customerDTO)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with id: {}", id);
        return customerService.deleteCustomer(id)
                .thenApply(v -> ResponseEntity.noContent().<Void>build());
    }
}

package com.boa.paydit.service.impl;

import com.boa.paydit.dto.CustomerDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Customer;
import com.boa.paydit.exception.ResourceNotFoundException;
import com.boa.paydit.exception.ValidationException;
import com.boa.paydit.repository.CustomerRepository;
import com.boa.paydit.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Customer Service Implementation with CompletableFuture
 */
@Slf4j
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ExecutorService executorService;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<CustomerDTO> createCustomer(CustomerDTO customerDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating customer with email: {}", customerDTO.getEmail());

            // Validation
            if (customerDTO.getEmail() == null || customerDTO.getEmail().isEmpty()) {
                throw new ValidationException("INVALID_EMAIL", "Email cannot be empty");
            }

            if (customerRepository.findByEmail(customerDTO.getEmail()).isPresent()) {
                throw new ValidationException("DUPLICATE_EMAIL", "Customer with this email already exists");
            }

            Customer customer = new Customer();
            customer.setName(customerDTO.getName());
            customer.setEmail(customerDTO.getEmail());
            customer.setPhone(customerDTO.getPhone());
            customer.setAccountBalance(customerDTO.getAccountBalance() != null ? customerDTO.getAccountBalance() : BigDecimal.ZERO);

            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer created with id: {}", savedCustomer.getId());

            return convertToDTO(savedCustomer);
        }, executorService);
    }

    @Override
    public CompletableFuture<CustomerDTO> updateCustomer(Long id, CustomerDTO customerDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Updating customer with id: {}", id);

            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                            "Customer not found with id: " + id));

            if (customerDTO.getName() != null && !customerDTO.getName().isEmpty()) {
                customer.setName(customerDTO.getName());
            }

            if (customerDTO.getPhone() != null) {
                customer.setPhone(customerDTO.getPhone());
            }

            if (customerDTO.getAccountBalance() != null) {
                customer.setAccountBalance(customerDTO.getAccountBalance());
            }

            Customer updatedCustomer = customerRepository.save(customer);
            log.info("Customer updated with id: {}", id);

            return convertToDTO(updatedCustomer);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<CustomerDTO> getCustomer(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching customer with id: {}", id);
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                            "Customer not found with id: " + id));
            return convertToDTO(customer);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<CustomerDTO>> getAllCustomers(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching all customers with pagination");
            Page<Customer> customers = customerRepository.findAll(pageable);
            return PageResponse.fromPage(customers.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<CustomerDTO>> searchCustomersByName(String name, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Searching customers by name: {}", name);
            Page<Customer> customers = customerRepository.findByNameContaining(name, pageable);
            return PageResponse.fromPage(customers.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> deleteCustomer(Long id) {
        return CompletableFuture.runAsync(() -> {
            log.info("Deleting customer with id: {}", id);
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("CUSTOMER_NOT_FOUND",
                            "Customer not found with id: " + id));
            customerRepository.delete(customer);
            log.info("Customer deleted with id: {}", id);
        }, executorService);
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAccountBalance(customer.getAccountBalance());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}

package com.boa.paydit.service.impl;

import com.boa.paydit.dto.CustomerDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Customer;
import com.boa.paydit.exception.ResourceNotFoundException;
import com.boa.paydit.exception.ValidationException;
import com.boa.paydit.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john@example.com");
        testCustomer.setPhone("1234567890");
        testCustomer.setAccountBalance(BigDecimal.valueOf(5000));
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setName("John Doe");
        testCustomerDTO.setEmail("john@example.com");
        testCustomerDTO.setPhone("1234567890");
        testCustomerDTO.setAccountBalance(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("Create customer successfully")
    void testCreateCustomerSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        CustomerDTO result = customerService.createCustomer(testCustomerDTO).get();

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create customer with duplicate email")
    void testCreateCustomerWithDuplicateEmail() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testCustomer));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            customerService.createCustomer(testCustomerDTO).get();
        });
    }

    @Test
    @DisplayName("Create customer with empty email")
    void testCreateCustomerWithEmptyEmail() throws ExecutionException, InterruptedException {
        // Arrange
        testCustomerDTO.setEmail("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            customerService.createCustomer(testCustomerDTO).get();
        });
    }

    @Test
    @DisplayName("Get customer successfully")
    void testGetCustomerSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // Act
        CustomerDTO result = customerService.getCustomer(1L).get();

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get customer not found")
    void testGetCustomerNotFound() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomer(1L).get();
        });
    }

    @Test
    @DisplayName("Get all customers with pagination")
    void testGetAllCustomers() throws ExecutionException, InterruptedException {
        // Arrange
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        Page<Customer> customerPage = new PageImpl<>(customers);
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        // Act
        PageResponse<CustomerDTO> result = customerService.getAllCustomers(pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Search customers by name")
    void testSearchCustomersByName() throws ExecutionException, InterruptedException {
        // Arrange
        List<Customer> customers = new ArrayList<>();
        customers.add(testCustomer);
        Page<Customer> customerPage = new PageImpl<>(customers);
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findByNameContaining("John", pageable)).thenReturn(customerPage);

        // Act
        PageResponse<CustomerDTO> result = customerService.searchCustomersByName("John", pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(customerRepository, times(1)).findByNameContaining("John", pageable);
    }

    @Test
    @DisplayName("Update customer successfully")
    void testUpdateCustomerSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerDTO updateDTO = new CustomerDTO();
        updateDTO.setName("Jane Doe");

        // Act
        CustomerDTO result = customerService.updateCustomer(1L, updateDTO).get();

        // Assert
        assertNotNull(result);
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Delete customer successfully")
    void testDeleteCustomerSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).delete(any(Customer.class));

        // Act
        customerService.deleteCustomer(1L).get();

        // Assert
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).delete(testCustomer);
    }
}

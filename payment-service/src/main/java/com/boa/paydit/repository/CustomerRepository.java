package com.boa.paydit.repository;

import com.boa.paydit.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customers by name with pagination
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * Find customers with payments
     */
    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.payments")
    Page<Customer> findAllWithPayments(Pageable pageable);

    /**
     * Find customers with balance greater than threshold
     */
    @Query("SELECT c FROM Customer c WHERE c.accountBalance > :threshold ORDER BY c.accountBalance DESC")
    Page<Customer> findByBalanceGreaterThan(@Param("threshold") java.math.BigDecimal threshold, Pageable pageable);
}

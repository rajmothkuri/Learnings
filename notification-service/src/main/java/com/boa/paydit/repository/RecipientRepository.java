package com.boa.paydit.repository;

import com.boa.paydit.entity.Recipient;
import com.boa.paydit.entity.NotificationPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Recipient entity
 */
@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    /**
     * Find recipient by email
     */
    Optional<Recipient> findByEmail(String email);

    /**
     * Find active recipients
     */
    @Query("SELECT r FROM Recipient r WHERE r.isActive = true")
    Page<Recipient> findActiveRecipients(Pageable pageable);

    /**
     * Find recipients by preference
     */
    @Query("SELECT r FROM Recipient r WHERE r.preference = :preference AND r.isActive = true")
    Page<Recipient> findByPreference(@Param("preference") NotificationPreference preference, Pageable pageable);

    /**
     * Find recipients with notifications
     */
    @Query("SELECT DISTINCT r FROM Recipient r LEFT JOIN FETCH r.notifications WHERE r.isActive = true")
    Page<Recipient> findActiveRecipientsWithNotifications(Pageable pageable);

    /**
     * Search recipients by name
     */
    @Query("SELECT r FROM Recipient r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')) AND r.isActive = true")
    Page<Recipient> searchByName(@Param("name") String name, Pageable pageable);
}

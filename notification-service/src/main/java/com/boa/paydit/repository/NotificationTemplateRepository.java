package com.boa.paydit.repository;

import com.boa.paydit.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for NotificationTemplate entity
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    /**
     * Find template by name
     */
    Optional<NotificationTemplate> findByName(String name);
}

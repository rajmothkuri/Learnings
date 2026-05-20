package com.boa.paydit.repository;

import com.boa.paydit.entity.Notification;
import com.boa.paydit.entity.NotificationChannel;
import com.boa.paydit.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity with custom queries
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by recipient
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    /**
     * Find notifications by status
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status ORDER BY n.createdAt DESC")
    Page<Notification> findByStatus(@Param("status") NotificationStatus status, Pageable pageable);

    /**
     * Find notifications by channel
     */
    @Query("SELECT n FROM Notification n WHERE n.channel = :channel ORDER BY n.createdAt DESC")
    Page<Notification> findByChannel(@Param("channel") NotificationChannel channel, Pageable pageable);

    /**
     * Find pending notifications for retry
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'RETRY' AND n.retryCount < :maxRetries ORDER BY n.createdAt ASC")
    List<Notification> findPendingRetries(@Param("maxRetries") int maxRetries);

    /**
     * Find notifications by date range with join
     */
    @Query("SELECT n FROM Notification n JOIN FETCH n.recipient WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    Page<Notification> findNotificationsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    /**
     * Find notifications by recipient and status
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.status = :status ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndStatus(@Param("recipientId") Long recipientId, @Param("status") NotificationStatus status, Pageable pageable);

    /**
     * Find sent notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'SENT' OR n.status = 'DELIVERED' ORDER BY n.sentDate DESC")
    Page<Notification> findSentNotifications(Pageable pageable);

    /**
     * Count notifications by status
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    long countByStatus(@Param("status") NotificationStatus status);

    /**
     * Find notifications by recipient with full fetch
     */
    @Query("SELECT DISTINCT n FROM Notification n LEFT JOIN FETCH n.recipient r LEFT JOIN FETCH n.template t WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdWithDetails(@Param("recipientId") Long recipientId, Pageable pageable);

    /**
     * Find failed notifications for error handling
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.errorMessage IS NOT NULL ORDER BY n.updatedAt ASC")
    Page<Notification> findFailedNotifications(Pageable pageable);
}

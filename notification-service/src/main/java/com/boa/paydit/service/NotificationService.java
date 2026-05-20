package com.boa.paydit.service;

import com.boa.paydit.dto.NotificationDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.NotificationStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    CompletableFuture<NotificationDTO> sendNotification(NotificationDTO notificationDTO);
    CompletableFuture<NotificationDTO> getNotification(Long id);
    CompletableFuture<PageResponse<NotificationDTO>> getAllNotifications(Pageable pageable);
    CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByRecipient(Long recipientId, Pageable pageable);
    CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByStatus(NotificationStatus status, Pageable pageable);
    CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    CompletableFuture<Void> retryFailedNotifications();
    CompletableFuture<Long> getFailedNotificationCount();
}

package com.boa.paydit.service.impl;

import com.boa.paydit.dto.NotificationDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Notification;
import com.boa.paydit.entity.NotificationStatus;
import com.boa.paydit.entity.Recipient;
import com.boa.paydit.entity.NotificationTemplate;
import com.boa.paydit.exception.NotificationProcessingException;
import com.boa.paydit.exception.InvalidNotificationException;
import com.boa.paydit.repository.NotificationRepository;
import com.boa.paydit.repository.RecipientRepository;
import com.boa.paydit.repository.NotificationTemplateRepository;
import com.boa.paydit.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Notification Service Implementation with CompletableFuture and async processing
 */
@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final RecipientRepository recipientRepository;
    private final NotificationTemplateRepository templateRepository;
    private final ExecutorService executorService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                 RecipientRepository recipientRepository,
                                 NotificationTemplateRepository templateRepository) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
        this.templateRepository = templateRepository;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<NotificationDTO> sendNotification(NotificationDTO notificationDTO) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending notification to recipient: {}", notificationDTO.getRecipientId());

            // Validation
            if (notificationDTO.getRecipientId() == null) {
                throw new InvalidNotificationException("INVALID_RECIPIENT", "Recipient ID cannot be null");
            }

            if (notificationDTO.getSubject() == null || notificationDTO.getSubject().isEmpty()) {
                throw new InvalidNotificationException("INVALID_SUBJECT", "Subject cannot be empty");
            }

            // Fetch recipient and template in parallel
            var recipientFuture = CompletableFuture.supplyAsync(() ->
                    recipientRepository.findById(notificationDTO.getRecipientId())
                            .orElseThrow(() -> new NotificationProcessingException("RECIPIENT_NOT_FOUND",
                                    "Recipient not found with id: " + notificationDTO.getRecipientId())), executorService);

            var templateFuture = CompletableFuture.supplyAsync(() ->
                    templateRepository.findById(notificationDTO.getTemplateId())
                            .orElseThrow(() -> new NotificationProcessingException("TEMPLATE_NOT_FOUND",
                                    "Template not found with id: " + notificationDTO.getTemplateId())), executorService);

            // Wait for both futures
            Recipient recipient = recipientFuture.join();
            NotificationTemplate template = templateFuture.join();

            // Create and save notification
            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setTemplate(template);
            notification.setSubject(notificationDTO.getSubject());
            notification.setMessage(notificationDTO.getMessage());
            notification.setChannel(null);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setRetryCount(0);

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification sent with id: {}", savedNotification.getId());

            return convertToDTO(savedNotification);
        }, executorService).exceptionally(ex -> {
            log.error("Error sending notification", ex);
            throw new NotificationProcessingException("SEND_NOTIFICATION_FAILED", 
                    "Failed to send notification: " + ex.getMessage(), ex);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<NotificationDTO> getNotification(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching notification with id: {}", id);
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new NotificationProcessingException("NOTIFICATION_NOT_FOUND",
                            "Notification not found with id: " + id));
            return convertToDTO(notification);
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<NotificationDTO>> getAllNotifications(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching all notifications with pagination");
            Page<Notification> notifications = notificationRepository.findAll(pageable);
            return PageResponse.fromPage(notifications.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByRecipient(Long recipientId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching notifications for recipient: {}", recipientId);
            Page<Notification> notifications = notificationRepository.findByRecipientId(recipientId, pageable);
            return PageResponse.fromPage(notifications.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching notifications with status: {}", status);
            Page<Notification> notifications = notificationRepository.findByStatus(status, pageable);
            return PageResponse.fromPage(notifications.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<PageResponse<NotificationDTO>> getNotificationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching notifications between {} and {}", startDate, endDate);
            Page<Notification> notifications = notificationRepository.findNotificationsByDateRange(startDate, endDate, pageable);
            return PageResponse.fromPage(notifications.map(this::convertToDTO));
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> retryFailedNotifications() {
        return CompletableFuture.runAsync(() -> {
            log.info("Processing failed notifications for retry");
            List<Notification> failedNotifications = notificationRepository.findPendingRetries(3);
            
            failedNotifications.forEach(notification -> {
                try {
                    notification.setRetryCount(notification.getRetryCount() + 1);
                    notification.setStatus(NotificationStatus.RETRY);
                    notificationRepository.save(notification);
                    log.info("Notification queued for retry: {}", notification.getId());
                } catch (Exception ex) {
                    log.error("Error retrying notification: {}", notification.getId(), ex);
                }
            });
        }, executorService);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Long> getFailedNotificationCount() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Counting failed notifications");
            return notificationRepository.countByStatus(NotificationStatus.FAILED);
        }, executorService);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setSubject(notification.getSubject());
        dto.setMessage(notification.getMessage());
        dto.setChannel(notification.getChannel() != null ? notification.getChannel().toString() : null);
        dto.setStatus(notification.getStatus().toString());
        dto.setSentDate(notification.getSentDate());
        dto.setRecipientId(notification.getRecipient().getId());
        dto.setTemplateId(notification.getTemplate().getId());
        dto.setRetryCount(notification.getRetryCount());
        dto.setErrorMessage(notification.getErrorMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
}

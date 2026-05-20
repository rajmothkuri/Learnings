package com.boa.paydit.controller;

import com.boa.paydit.dto.NotificationDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.NotificationStatus;
import com.boa.paydit.kafka.NotificationProducer;
import com.boa.paydit.dto.KafkaNotificationEvent;
import com.boa.paydit.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Notification Controller with async endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    public NotificationController(NotificationService notificationService, NotificationProducer notificationProducer) {
        this.notificationService = notificationService;
        this.notificationProducer = notificationProducer;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<NotificationDTO>> sendNotification(
            @RequestBody NotificationDTO notificationDTO) {
        log.info("Sending notification");
        return notificationService.sendNotification(notificationDTO)
                .thenApply(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @PostMapping("/async/kafka")
    public CompletableFuture<ResponseEntity<String>> sendNotificationViaKafka(
            @RequestBody KafkaNotificationEvent event) {
        log.info("Sending notification via Kafka: recipient={}", event.getRecipientId());
        return notificationProducer.sendPaymentNotificationAsync(event)
                .thenApply(v -> ResponseEntity.status(HttpStatus.ACCEPTED).body("Notification queued for processing"))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to queue notification"));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<NotificationDTO>> getNotification(@PathVariable Long id) {
        log.info("Getting notification with id: {}", id);
        return notificationService.getNotification(id)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<PageResponse<NotificationDTO>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting all notifications");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return notificationService.getAllNotifications(pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/recipient/{recipientId}")
    public CompletableFuture<ResponseEntity<PageResponse<NotificationDTO>>> getByRecipient(
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting notifications for recipient: {}", recipientId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return notificationService.getNotificationsByRecipient(recipientId, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/status/{status}")
    public CompletableFuture<ResponseEntity<PageResponse<NotificationDTO>>> getByStatus(
            @PathVariable NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting notifications with status: {}", status);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return notificationService.getNotificationsByStatus(status, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/date-range")
    public CompletableFuture<ResponseEntity<PageResponse<NotificationDTO>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        log.info("Getting notifications between {} and {}", startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return notificationService.getNotificationsByDateRange(startDate, endDate, pageable)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/retry-failed")
    public CompletableFuture<ResponseEntity<String>> retryFailedNotifications() {
        log.info("Retrying failed notifications");
        return notificationService.retryFailedNotifications()
                .thenApply(v -> ResponseEntity.ok("Failed notifications retry initiated"))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retry notifications"));
    }

    @GetMapping("/failed/count")
    public CompletableFuture<ResponseEntity<Long>> getFailedCount() {
        log.info("Getting failed notification count");
        return notificationService.getFailedNotificationCount()
                .thenApply(ResponseEntity::ok);
    }
}

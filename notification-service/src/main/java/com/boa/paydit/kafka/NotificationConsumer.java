package com.boa.paydit.kafka;

import com.boa.paydit.dto.KafkaNotificationEvent;
import com.boa.paydit.entity.Notification;
import com.boa.paydit.entity.NotificationChannel;
import com.boa.paydit.entity.NotificationStatus;
import com.boa.paydit.entity.Recipient;
import com.boa.paydit.entity.NotificationTemplate;
import com.boa.paydit.exception.NotificationProcessingException;
import com.boa.paydit.repository.NotificationRepository;
import com.boa.paydit.repository.RecipientRepository;
import com.boa.paydit.repository.NotificationTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Async Kafka Consumer for notifications with error handling and retry logic
 */
@Slf4j
@Service
@Transactional
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final RecipientRepository recipientRepository;
    private final NotificationTemplateRepository templateRepository;

    public NotificationConsumer(NotificationRepository notificationRepository,
                               RecipientRepository recipientRepository,
                               NotificationTemplateRepository templateRepository) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
        this.templateRepository = templateRepository;
    }

    /**
     * Listen for payment notifications
     */
    @KafkaListener(topics = "payment-notifications", groupId = "notification-service-group", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentNotification(@Payload KafkaNotificationEvent event,
                                          @Header(name = "notification_type", required = false) String notificationType,
                                          Acknowledgment acknowledgment) {
        log.info("Received payment notification event: recipient={}, template={}", 
                event.getRecipientId(), event.getTemplateName());

        try {
            processNotificationEvent(event, NotificationChannel.EMAIL);
            acknowledgment.acknowledge();
            log.info("Payment notification processed successfully");
        } catch (Exception ex) {
            log.error("Error processing payment notification: {}", ex.getMessage(), ex);
            handleNotificationError(event, ex);
        }
    }

    /**
     * Listen for system notifications
     */
    @KafkaListener(topics = "system-notifications", groupId = "notification-service-group", 
                   containerFactory = "kafkaListenerContainerFactory")
    public void consumeSystemNotification(@Payload KafkaNotificationEvent event,
                                         @Header(name = "notification_type", required = false) String notificationType,
                                         Acknowledgment acknowledgment) {
        log.info("Received system notification event: recipient={}, template={}", 
                event.getRecipientId(), event.getTemplateName());

        try {
            processNotificationEvent(event, NotificationChannel.IN_APP);
            acknowledgment.acknowledge();
            log.info("System notification processed successfully");
        } catch (Exception ex) {
            log.error("Error processing system notification: {}", ex.getMessage(), ex);
            handleNotificationError(event, ex);
        }
    }

    /**
     * Process notification event asynchronously
     */
    private CompletableFuture<Void> processNotificationEvent(KafkaNotificationEvent event, NotificationChannel channel) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Fetch recipient
                Optional<Recipient> recipientOpt = recipientRepository.findById(event.getRecipientId());
                if (recipientOpt.isEmpty()) {
                    log.warn("Recipient not found: {}", event.getRecipientId());
                    throw new NotificationProcessingException("RECIPIENT_NOT_FOUND", 
                            "Recipient not found: " + event.getRecipientId());
                }

                Recipient recipient = recipientOpt.get();

                // Fetch template
                Optional<NotificationTemplate> templateOpt = templateRepository.findByName(event.getTemplateName());
                if (templateOpt.isEmpty()) {
                    log.warn("Template not found: {}", event.getTemplateName());
                    throw new NotificationProcessingException("TEMPLATE_NOT_FOUND", 
                            "Template not found: " + event.getTemplateName());
                }

                NotificationTemplate template = templateOpt.get();

                // Create notification entity
                Notification notification = new Notification();
                notification.setRecipient(recipient);
                notification.setTemplate(template);
                notification.setSubject(event.getSubject());
                notification.setMessage(event.getMessage());
                notification.setChannel(channel);
                notification.setStatus(NotificationStatus.PENDING);
                notification.setRetryCount(0);

                // Save notification
                Notification savedNotification = notificationRepository.save(notification);

                // Update status to SENT
                savedNotification.setStatus(NotificationStatus.SENT);
                savedNotification.setSentDate(LocalDateTime.now());
                notificationRepository.save(savedNotification);

                log.info("Notification processed and saved: id={}", savedNotification.getId());
            } catch (Exception ex) {
                log.error("Error in processNotificationEvent: {}", ex.getMessage(), ex);
                throw new NotificationProcessingException("NOTIFICATION_PROCESS_FAILED", 
                        "Failed to process notification: " + ex.getMessage(), ex);
            }
        });
    }

    /**
     * Handle notification errors with retry logic
     */
    private void handleNotificationError(KafkaNotificationEvent event, Exception ex) {
        log.error("Handling notification error: {}", ex.getMessage());
        
        try {
            Optional<Recipient> recipientOpt = recipientRepository.findById(event.getRecipientId());
            Optional<NotificationTemplate> templateOpt = templateRepository.findByName(event.getTemplateName());

            if (recipientOpt.isPresent() && templateOpt.isPresent()) {
                Notification notification = new Notification();
                notification.setRecipient(recipientOpt.get());
                notification.setTemplate(templateOpt.get());
                notification.setSubject(event.getSubject());
                notification.setMessage(event.getMessage());
                notification.setChannel(NotificationChannel.EMAIL);
                notification.setStatus(NotificationStatus.RETRY);
                notification.setRetryCount(0);
                notification.setErrorMessage(ex.getMessage());

                notificationRepository.save(notification);
                log.info("Notification saved for retry");
            }
        } catch (Exception retryEx) {
            log.error("Failed to save notification for retry: {}", retryEx.getMessage(), retryEx);
        }
    }
}

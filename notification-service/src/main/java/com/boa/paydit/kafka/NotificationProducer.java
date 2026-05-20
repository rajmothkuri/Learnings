package com.boa.paydit.kafka;

import com.boa.paydit.dto.KafkaNotificationEvent;
import com.boa.paydit.exception.NotificationSendException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Async Kafka Producer for notifications
 */
@Slf4j
@Service
public class NotificationProducer {

    private static final String TOPIC_PAYMENT_NOTIFICATION = "payment-notifications";
    private static final String TOPIC_SYSTEM_NOTIFICATION = "system-notifications";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send payment notification asynchronously
     */
    public CompletableFuture<Void> sendPaymentNotificationAsync(KafkaNotificationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Publishing payment notification event to topic: {}", TOPIC_PAYMENT_NOTIFICATION);

                Message<KafkaNotificationEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, TOPIC_PAYMENT_NOTIFICATION)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, String.valueOf(event.getRecipientId()))
                        .setHeader("notification_type", "PAYMENT")
                        .setHeader("timestamp", System.currentTimeMillis())
                        .build();

                kafkaTemplate.send(message)
                        .thenApply(result -> {
                            log.info("Payment notification published successfully: partition={}, offset={}", 
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                            return result;
                        })
                        .exceptionally(ex -> {
                            log.error("Failed to publish payment notification: {}", ex.getMessage(), ex);
                            throw new NotificationSendException("KAFKA_SEND_FAILED", 
                                    "Failed to send payment notification: " + ex.getMessage(), ex);
                        });
            } catch (Exception ex) {
                log.error("Error in sendPaymentNotificationAsync", ex);
                throw new NotificationSendException("PAYMENT_NOTIFICATION_FAILED", 
                        "Failed to send payment notification", ex);
            }
        });
    }

    /**
     * Send system notification asynchronously
     */
    public CompletableFuture<Void> sendSystemNotificationAsync(KafkaNotificationEvent event) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Publishing system notification event to topic: {}", TOPIC_SYSTEM_NOTIFICATION);

                Message<KafkaNotificationEvent> message = MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, TOPIC_SYSTEM_NOTIFICATION)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, String.valueOf(event.getRecipientId()))
                        .setHeader("notification_type", "SYSTEM")
                        .setHeader("timestamp", System.currentTimeMillis())
                        .build();

                kafkaTemplate.send(message)
                        .thenApply(result -> {
                            log.info("System notification published successfully: partition={}, offset={}", 
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                            return result;
                        })
                        .exceptionally(ex -> {
                            log.error("Failed to publish system notification: {}", ex.getMessage(), ex);
                            throw new NotificationSendException("KAFKA_SEND_FAILED", 
                                    "Failed to send system notification: " + ex.getMessage(), ex);
                        });
            } catch (Exception ex) {
                log.error("Error in sendSystemNotificationAsync", ex);
                throw new NotificationSendException("SYSTEM_NOTIFICATION_FAILED", 
                        "Failed to send system notification", ex);
            }
        });
    }

    /**
     * Batch send notifications asynchronously
     */
    public CompletableFuture<Void> batchSendNotificationsAsync(java.util.List<KafkaNotificationEvent> events) {
        return CompletableFuture.allOf(
                events.stream()
                        .map(this::sendPaymentNotificationAsync)
                        .toArray(CompletableFuture[]::new)
        );
    }
}

package com.boa.paydit.kafka;

import com.boa.paydit.dto.KafkaNotificationEvent;
import com.boa.paydit.exception.NotificationSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationProducer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Producer Tests")
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    private KafkaNotificationEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new KafkaNotificationEvent(
                1L,
                "PAYMENT_NOTIFICATION",
                "Payment Received",
                "Your payment has been received",
                "EMAIL"
        );
    }

    @Test
    @DisplayName("Send payment notification successfully")
    void testSendPaymentNotificationSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(kafkaTemplate.send(any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new org.springframework.kafka.support.KafkaNull()));

        // Act
        notificationProducer.sendPaymentNotificationAsync(testEvent).get();

        // Assert
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("Send system notification successfully")
    void testSendSystemNotificationSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(kafkaTemplate.send(any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new org.springframework.kafka.support.KafkaNull()));

        // Act
        notificationProducer.sendSystemNotificationAsync(testEvent).get();

        // Assert
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }
}

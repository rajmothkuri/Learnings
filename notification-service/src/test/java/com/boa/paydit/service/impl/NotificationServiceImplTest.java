package com.boa.paydit.service.impl;

import com.boa.paydit.dto.NotificationDTO;
import com.boa.paydit.dto.PageResponse;
import com.boa.paydit.entity.Notification;
import com.boa.paydit.entity.NotificationChannel;
import com.boa.paydit.entity.NotificationStatus;
import com.boa.paydit.entity.Recipient;
import com.boa.paydit.entity.NotificationTemplate;
import com.boa.paydit.exception.InvalidNotificationException;
import com.boa.paydit.exception.NotificationProcessingException;
import com.boa.paydit.repository.NotificationRepository;
import com.boa.paydit.repository.RecipientRepository;
import com.boa.paydit.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Recipient testRecipient;
    private NotificationTemplate testTemplate;
    private Notification testNotification;
    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testRecipient = new Recipient();
        testRecipient.setId(1L);
        testRecipient.setName("John Doe");
        testRecipient.setEmail("john@example.com");
        testRecipient.setIsActive(true);

        testTemplate = new NotificationTemplate();
        testTemplate.setId(1L);
        testTemplate.setName("PAYMENT_NOTIFICATION");
        testTemplate.setSubject("Payment Received");
        testTemplate.setTemplateContent("Your payment has been received");

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setSubject("Payment Received");
        testNotification.setMessage("Your payment has been received");
        testNotification.setChannel(NotificationChannel.EMAIL);
        testNotification.setStatus(NotificationStatus.SENT);
        testNotification.setRecipient(testRecipient);
        testNotification.setTemplate(testTemplate);
        testNotification.setRetryCount(0);

        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setRecipientId(1L);
        testNotificationDTO.setTemplateId(1L);
        testNotificationDTO.setSubject("Payment Received");
        testNotificationDTO.setMessage("Your payment has been received");
    }

    @Test
    @DisplayName("Send notification successfully")
    void testSendNotificationSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(recipientRepository.findById(1L)).thenReturn(Optional.of(testRecipient));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        NotificationDTO result = notificationService.sendNotification(testNotificationDTO).get();

        // Assert
        assertNotNull(result);
        assertEquals("Payment Received", result.getSubject());
        verify(recipientRepository, times(1)).findById(1L);
        verify(templateRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Send notification with null recipient")
    void testSendNotificationWithNullRecipient() throws ExecutionException, InterruptedException {
        // Arrange
        testNotificationDTO.setRecipientId(null);

        // Act & Assert
        assertThrows(InvalidNotificationException.class, () -> {
            notificationService.sendNotification(testNotificationDTO).get();
        });
    }

    @Test
    @DisplayName("Send notification with non-existent recipient")
    void testSendNotificationWithNonExistentRecipient() throws ExecutionException, InterruptedException {
        // Arrange
        when(recipientRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotificationProcessingException.class, () -> {
            notificationService.sendNotification(testNotificationDTO).get();
        });
    }

    @Test
    @DisplayName("Get notification successfully")
    void testGetNotificationSuccess() throws ExecutionException, InterruptedException {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // Act
        NotificationDTO result = notificationService.getNotification(1L).get();

        // Assert
        assertNotNull(result);
        assertEquals("Payment Received", result.getSubject());
        verify(notificationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get all notifications with pagination")
    void testGetAllNotifications() throws ExecutionException, InterruptedException {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 10);

        when(notificationRepository.findAll(pageable)).thenReturn(notificationPage);

        // Act
        PageResponse<NotificationDTO> result = notificationService.getAllNotifications(pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(notificationRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Get notifications by recipient")
    void testGetNotificationsByRecipient() throws ExecutionException, InterruptedException {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 10);

        when(notificationRepository.findByRecipientId(1L, pageable)).thenReturn(notificationPage);

        // Act
        PageResponse<NotificationDTO> result = notificationService.getNotificationsByRecipient(1L, pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(notificationRepository, times(1)).findByRecipientId(1L, pageable);
    }

    @Test
    @DisplayName("Get notifications by status")
    void testGetNotificationsByStatus() throws ExecutionException, InterruptedException {
        // Arrange
        List<Notification> notifications = new ArrayList<>();
        notifications.add(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 10);

        when(notificationRepository.findByStatus(NotificationStatus.SENT, pageable)).thenReturn(notificationPage);

        // Act
        PageResponse<NotificationDTO> result = notificationService.getNotificationsByStatus(NotificationStatus.SENT, pageable).get();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(notificationRepository, times(1)).findByStatus(NotificationStatus.SENT, pageable);
    }

    @Test
    @DisplayName("Retry failed notifications")
    void testRetryFailedNotifications() throws ExecutionException, InterruptedException {
        // Arrange
        List<Notification> failedNotifications = new ArrayList<>();
        failedNotifications.add(testNotification);

        when(notificationRepository.findPendingRetries(3)).thenReturn(failedNotifications);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.retryFailedNotifications().get();

        // Assert
        verify(notificationRepository, times(1)).findPendingRetries(3);
        verify(notificationRepository, times(failedNotifications.size())).save(any(Notification.class));
    }

    @Test
    @DisplayName("Get failed notification count")
    void testGetFailedNotificationCount() throws ExecutionException, InterruptedException {
        // Arrange
        when(notificationRepository.countByStatus(NotificationStatus.FAILED)).thenReturn(5L);

        // Act
        Long result = notificationService.getFailedNotificationCount().get();

        // Assert
        assertEquals(5L, result);
        verify(notificationRepository, times(1)).countByStatus(NotificationStatus.FAILED);
    }
}

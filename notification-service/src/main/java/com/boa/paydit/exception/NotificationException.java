package com.boa.paydit.exception;

/**
 * Sealed interface for notification app exceptions
 */
public sealed interface NotificationException permits NotificationSendException, InvalidNotificationException, NotificationProcessingException {
    String getErrorCode();
    String getMessage();
    int getHttpStatus();
}

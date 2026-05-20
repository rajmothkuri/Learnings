package com.boa.paydit.exception;

/**
 * Sealed interface for application exceptions
 * Restricts implementations to specific exception types
 */
public sealed interface AppException permits PaymentException, ValidationException, ResourceNotFoundException, ConcurrencyException {
    String getErrorCode();
    String getMessage();
    int getHttpStatus();
}

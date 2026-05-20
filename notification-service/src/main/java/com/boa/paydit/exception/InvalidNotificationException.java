package com.boa.paydit.exception;

public final class InvalidNotificationException extends RuntimeException implements NotificationException {
    private final String errorCode;
    private final int httpStatus;

    public InvalidNotificationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}

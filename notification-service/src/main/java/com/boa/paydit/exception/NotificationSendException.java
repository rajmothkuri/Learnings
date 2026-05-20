package com.boa.paydit.exception;

public final class NotificationSendException extends RuntimeException implements NotificationException {
    private final String errorCode;
    private final int httpStatus;

    public NotificationSendException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 500;
    }

    public NotificationSendException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = 500;
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

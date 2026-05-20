package com.boa.paydit.exception;

/**
 * Payment-specific exception
 */
public final class PaymentException extends RuntimeException implements AppException {
    private final String errorCode;
    private final int httpStatus;

    public PaymentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 400;
    }

    public PaymentException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public PaymentException(String errorCode, String message, Throwable cause) {
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

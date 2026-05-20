package com.boa.paydit.exception;

public final class ConcurrencyException extends RuntimeException implements AppException {
    private final String errorCode;
    private final int httpStatus;

    public ConcurrencyException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 409;
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

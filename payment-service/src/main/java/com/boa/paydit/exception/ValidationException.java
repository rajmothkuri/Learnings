package com.boa.paydit.exception;

public final class ValidationException extends RuntimeException implements AppException {
    private final String errorCode;
    private final int httpStatus;

    public ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 422;
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

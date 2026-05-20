package com.boa.paydit.exception;

public final class ResourceNotFoundException extends RuntimeException implements AppException {
    private final String errorCode;
    private final int httpStatus;

    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 404;
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

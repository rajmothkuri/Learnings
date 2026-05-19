package com.boa.paydit.exception;

public final class InvalidCredentialsException
extends RuntimeException
implements AuthException {

    public InvalidCredentialsException(
        String message) {

        super(message);
    }
}
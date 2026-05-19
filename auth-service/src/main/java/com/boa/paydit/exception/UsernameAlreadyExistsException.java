package com.boa.paydit.exception;

public final class UsernameAlreadyExistsException
    extends RuntimeException
    implements AuthException {

    public UsernameAlreadyExistsException(
        String message) {

        super(message);
    }
}
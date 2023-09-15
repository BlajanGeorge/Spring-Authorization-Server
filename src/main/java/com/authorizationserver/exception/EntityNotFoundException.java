package com.authorizationserver.exception;

/**
 * Exception for not found data
 *
 * @author Blajan George
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}

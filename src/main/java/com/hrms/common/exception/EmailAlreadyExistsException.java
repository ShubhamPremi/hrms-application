package com.hrms.common.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("An employee with email '" + email + "' already exists");
    }
}
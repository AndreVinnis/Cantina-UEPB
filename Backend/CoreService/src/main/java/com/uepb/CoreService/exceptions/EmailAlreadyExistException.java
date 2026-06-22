package com.uepb.CoreService.exceptions;

public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException(String email) {
        super("Esse email já está cadastrado no sistema! Email: " + email);
    }
}

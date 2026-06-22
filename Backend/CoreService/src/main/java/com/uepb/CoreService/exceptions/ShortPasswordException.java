package com.uepb.CoreService.exceptions;

public class ShortPasswordException extends RuntimeException {
    public ShortPasswordException() {
        super("A senha deve ter no mínimo 8 caracteres");
    }
}

package com.uepb.CoreService.exceptions;

public class CafeteriaNotFound extends RuntimeException {
    public CafeteriaNotFound(String email) {
        super("A lanchonete não foi encontrada: " + email);
    }
}

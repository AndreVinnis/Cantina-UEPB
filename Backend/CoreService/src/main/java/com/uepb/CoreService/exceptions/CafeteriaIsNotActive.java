package com.uepb.CoreService.exceptions;

public class CafeteriaIsNotActive extends RuntimeException {
    public CafeteriaIsNotActive(String name) {
        super("A lanchonete não está ativa: " + name);
    }
}

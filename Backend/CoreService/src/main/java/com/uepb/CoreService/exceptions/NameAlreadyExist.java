package com.uepb.CoreService.exceptions;

public class NameAlreadyExist extends RuntimeException {
    public NameAlreadyExist(String name) {
        super("Esse nome já existe nesse campus: " + name);
    }
}

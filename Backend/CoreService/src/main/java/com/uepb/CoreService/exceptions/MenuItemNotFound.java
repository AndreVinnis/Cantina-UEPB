package com.uepb.CoreService.exceptions;

public class MenuItemNotFound extends RuntimeException {
    public MenuItemNotFound(String name) {
        super("O item não foi encontrado: " + name);
    }
}

package com.uepb.CoreService.exceptions;

public class MenuItemAlreadyExists extends RuntimeException {
    public MenuItemAlreadyExists(String name) {
        super("Esse item já está no menu: " + name);
    }
}

package com.uepb.CoreService.exceptions;

public class NoMenuItemsYet extends RuntimeException {
    public NoMenuItemsYet() {
        super("Ainda não há itens nessa lanchonete");
    }
}

package com.uepb.OrderService.exception;

public class NoOrdersYet extends RuntimeException {
    public NoOrdersYet() {
        super("Não há pedidos em aberto.");
    }
}

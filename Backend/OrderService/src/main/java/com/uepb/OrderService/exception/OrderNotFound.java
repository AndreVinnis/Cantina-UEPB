package com.uepb.OrderService.exception;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(String id) {
        super("O pedido não foi encontrado. ID: " + id);
    }
}

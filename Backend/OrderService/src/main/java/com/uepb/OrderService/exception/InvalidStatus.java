package com.uepb.OrderService.exception;

import com.uepb.OrderService.enums.Status;

public class InvalidStatus extends RuntimeException {
    public InvalidStatus(Status currentStatus, Status newStatus) {
        super("Não é possível alterar um pedido com status: " + currentStatus + " para o status: " + newStatus);
    }
}

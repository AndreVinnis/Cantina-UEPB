package com.uepb.PaymentService.exceptions;

public class BillingNotFound extends RuntimeException {
    public BillingNotFound(String id) {
        super("Nenhuma cobrança encontrada com o id: " + id);
    }
}

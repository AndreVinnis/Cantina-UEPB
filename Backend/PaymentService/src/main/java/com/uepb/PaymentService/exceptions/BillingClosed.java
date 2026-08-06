package com.uepb.PaymentService.exceptions;

public class BillingClosed extends RuntimeException {
    public BillingClosed(String id) {
        super("Essa cobrança já foi encerrada. Id: " + id);
    }
}

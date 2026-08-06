package com.uepb.PaymentService.exceptions;

public class BillingExpired extends RuntimeException {
    public BillingExpired(String id) {
        super("Cobrança expirada. Id: " + id);
    }
}

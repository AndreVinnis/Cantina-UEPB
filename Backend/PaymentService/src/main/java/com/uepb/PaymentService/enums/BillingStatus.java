package com.uepb.PaymentService.enums;

public enum BillingStatus {
    PENDING,
    PAID,
    EXPIRED,
    CANCELLED,
    REFUNDED,
    UNKNOWN;


    public static BillingStatus fromAbacatePay(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return UNKNOWN;
        }
        try {
            return BillingStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /** Indica se o status é terminal, ou seja, não deve mais mudar. */
    public boolean isFinal() {
        return this == PAID || this == EXPIRED || this == CANCELLED || this == REFUNDED;
    }

    public boolean isPaid() {
        return this == PAID;
    }
}
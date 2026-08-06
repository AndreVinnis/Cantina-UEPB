package com.uepb.PaymentService.integration.dto.response;

public record AbacatePayResponse<T>(
        T data,
        boolean success,
        String error
) {}

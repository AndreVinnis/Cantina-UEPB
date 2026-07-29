package com.uepb.OrderService.integration;

import java.math.BigDecimal;

public record ItemValidatedResponse(
        String id,
        String name,
        Integer quantity,
        BigDecimal uniquePrice
) {
}

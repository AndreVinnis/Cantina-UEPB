package com.uepb.OrderService.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String itemName,
        Integer quantity,
        BigDecimal uniquePrice,
        BigDecimal total
) {
}

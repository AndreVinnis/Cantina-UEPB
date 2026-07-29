package com.uepb.OrderService.dto.request;

public record OrderItemRequest(
        String productName,
        Integer quantity
) {
}

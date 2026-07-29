package com.uepb.CoreService.dto.request;

public record OrderItemRequest(
        String productName,
        Integer quantity
) {
}

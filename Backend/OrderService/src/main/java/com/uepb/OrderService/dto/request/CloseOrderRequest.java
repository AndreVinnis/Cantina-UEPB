package com.uepb.OrderService.dto.request;

public record CloseOrderRequest(
        String orderId,
        String code
) {
}

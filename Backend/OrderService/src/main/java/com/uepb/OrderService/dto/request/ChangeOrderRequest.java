package com.uepb.OrderService.dto.request;

import com.uepb.OrderService.enums.Status;

public record ChangeOrderRequest(
        String orderId,
        Status newStatus
) {
}

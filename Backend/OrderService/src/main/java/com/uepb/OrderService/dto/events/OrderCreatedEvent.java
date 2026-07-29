package com.uepb.OrderService.dto.events;

import com.uepb.OrderService.enums.PaymentMethod;

public record OrderCreatedEvent(
        String orderId,
        PaymentMethod paymentMethod
) {
}

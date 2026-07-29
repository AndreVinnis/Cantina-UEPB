package com.uepb.OrderService.dto.request;

import com.uepb.OrderService.enums.PaymentMethod;

import java.util.List;

public record OrderRequest(
        String clientName,
        String cafeteriaId,
        String cafeteriaName,
        List<OrderItemRequest> items,
        PaymentMethod paymentMethod
) {
}

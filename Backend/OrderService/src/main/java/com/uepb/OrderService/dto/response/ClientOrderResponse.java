package com.uepb.OrderService.dto.response;

import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ClientOrderResponse(
        String sessionToken,
        String cafeteriaName,
        List<OrderItemResponse> items,
        BigDecimal totalPrice,
        PaymentMethod paymentMethod,
        Status status,
        Instant createdAt
) {
}

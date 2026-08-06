package com.uepb.PaymentService.dto.request;

import java.math.BigDecimal;

public record PaymentRequest(
        String orderId,
        String cafeteriaId,
        BigDecimal totalAmount,
        String clientName
) {
}

package com.uepb.CoreService.dto.response;

import java.math.BigDecimal;

public record ItemsResponse(
        String id,
        String name,
        Integer quantity,
        BigDecimal uniquePrice
) {
}

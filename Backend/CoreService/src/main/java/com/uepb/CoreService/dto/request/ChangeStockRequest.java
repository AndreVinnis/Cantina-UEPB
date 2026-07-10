package com.uepb.CoreService.dto.request;

public record ChangeStockRequest(
        String itemName,
        Integer quantity
) {
}

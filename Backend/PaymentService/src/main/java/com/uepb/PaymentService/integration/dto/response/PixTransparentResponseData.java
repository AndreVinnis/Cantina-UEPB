package com.uepb.PaymentService.integration.dto.response;

public record PixTransparentResponseData(
        String id,
        String status,
        String brCode,
        String brCodeBase64,
        String expiresAt
) {}

package com.uepb.PaymentService.integration.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record PixTransparentRequest(
        String method, // Sempre enviar "PIX"
        PixData data
) {
    public record PixData(
            Integer amount, // Valor em centavos
            String description,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Customer customer,
            Map<String, String> metadata
    ) {}

    public record Customer(
            String name,
            String email,
            String taxId,
            String cellphone
    ) {}
}

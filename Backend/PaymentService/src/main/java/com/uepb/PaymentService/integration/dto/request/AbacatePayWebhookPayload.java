package com.uepb.PaymentService.integration.dto.request;

import java.util.Map;

public record AbacatePayWebhookPayload(
        String eventId,   // ID único deste aviso (ex: evt_98765)
        String event,     // Tipo do evento (ex: "payment.paid")
        WebhookData data
) {
    public record WebhookData(
            String id,          // ID da transação no AbacatePay
            String status,      // "PAID", "EXPIRED", etc
            Map metadata // Aqui virá o seu "id_pedido_interno"
    ) {}
}

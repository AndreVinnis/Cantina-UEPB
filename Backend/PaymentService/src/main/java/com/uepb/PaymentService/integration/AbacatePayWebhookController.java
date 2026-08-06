package com.uepb.PaymentService.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uepb.PaymentService.integration.dto.request.AbacatePayWebhookPayload;
import com.uepb.PaymentService.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/abacatepay")
public class AbacatePayWebhookController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity receberWebhook(
            @RequestHeader("X-AbacatePay-Signature") String signature,
            @RequestBody String rawPayload
    ) {
        if (!billingService.isValidSignature(rawPayload, signature)) {
            log.warn("Tentativa de webhook com assinatura inválida bloqueada!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // 2. Agora que sabemos que é seguro, convertemos para o objeto Java
            AbacatePayWebhookPayload payload = objectMapper.readValue(
                    rawPayload,
                    AbacatePayWebhookPayload.class
            );

            // 3. Processa a regra de negócio
            billingService.processPayment(payload);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Erro ao processar o payload do webhook", e);
            // Retorna 200 OK mesmo com erro de parse interno para o AbacatePay
            // não ficar re-tentando enviar um payload que seu sistema não entende
            return ResponseEntity.ok().build();
        }
    }
}

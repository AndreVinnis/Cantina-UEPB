package com.uepb.PaymentService.integration;

import com.uepb.PaymentService.integration.dto.request.PixTransparentRequest;
import com.uepb.PaymentService.integration.dto.response.AbacatePayResponse;
import com.uepb.PaymentService.integration.dto.response.PixTransparentResponseData;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AbacatePayFallbackFactory implements FallbackFactory<AbacatePayClient> {

    @Override
    public AbacatePayClient create(Throwable cause) {
        return new AbacatePayClient() {
            @Override
            public AbacatePayResponse<PixTransparentResponseData> createPix(String token, PixTransparentRequest request) {
                log.error("Falha ao comunicar com AbacatePay. Motivo: {}", cause.getMessage());

                return new AbacatePayResponse<>(
                        null,
                        false,
                        "Gateway de pagamento indisponível no momento. Tente novamente mais tarde."
                );
            }
        };
    }
}

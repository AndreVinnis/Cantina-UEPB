package com.uepb.PaymentService.integration;

import com.uepb.PaymentService.integration.dto.request.PixTransparentRequest;
import com.uepb.PaymentService.integration.dto.response.AbacatePayResponse;
import com.uepb.PaymentService.integration.dto.response.PixTransparentResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "abacatePayClient",
        url = "https://api.abacatepay.com",
        fallbackFactory = AbacatePayFallbackFactory.class
)
public interface AbacatePayClient {

    // Rota de Checkout Transparente (Gera o PIX direto)
    @PostMapping(value = "/v2/transparents/create")
    AbacatePayResponse<PixTransparentResponseData> createPix(
            @RequestHeader("Authorization") String token,
            @RequestBody PixTransparentRequest request
    );
}

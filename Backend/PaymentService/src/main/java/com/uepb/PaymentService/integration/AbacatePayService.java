package com.uepb.PaymentService.integration;

import com.uepb.PaymentService.dto.request.PaymentRequest;
import com.uepb.PaymentService.integration.dto.request.PixTransparentRequest;
import com.uepb.PaymentService.integration.dto.response.PixTransparentResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AbacatePayService {

    @Autowired
    private AbacatePayClient abacatePayClientclient;

    @Value("${abacatepay.api.key}")
    private String apiKey;

    public PixTransparentResponseData generatePix(PaymentRequest paymentRequest) {
        String cleanApiKey = apiKey.replace("\"", "").replace("'", "").trim();
        String token = "Bearer " + cleanApiKey;


        int valorEmCentavos = paymentRequest.totalAmount().intValue() * 100;
        var metadata = Map.of("id_pedido_interno", paymentRequest.orderId());
        var pixData = new PixTransparentRequest.PixData(
                valorEmCentavos,
                "Pagamento do pedido " + paymentRequest.orderId(),
                null,
                metadata
        );
        var request = new PixTransparentRequest("PIX", pixData);
        var response = abacatePayClientclient.createPix(token, request);
        return response.data();
    }
}

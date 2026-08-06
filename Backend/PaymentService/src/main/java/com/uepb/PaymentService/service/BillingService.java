package com.uepb.PaymentService.service;

import com.uepb.PaymentService.domain.Billing;
import com.uepb.PaymentService.dto.request.PaymentRequest;
import com.uepb.PaymentService.enums.BillingStatus;
import com.uepb.PaymentService.exceptions.BillingClosed;
import com.uepb.PaymentService.exceptions.BillingExpired;
import com.uepb.PaymentService.exceptions.BillingNotFound;
import com.uepb.PaymentService.integration.AbacatePayService;
import com.uepb.PaymentService.integration.dto.request.AbacatePayWebhookPayload;
import com.uepb.PaymentService.integration.dto.request.PixTransparentRequest;
import com.uepb.PaymentService.integration.dto.response.PixTransparentResponseData;
import com.uepb.PaymentService.repository.BillingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Service
public class BillingService {

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private AbacatePayService abacatePayService;

    @Value("${abacatepay.webhook.secret}")
    private String webhookSecret;

    @Transactional
    public PixTransparentResponseData createBillingPix(PaymentRequest paymentRequest){
        PixTransparentResponseData pixResponse = abacatePayService.generatePix(paymentRequest);
        System.out.println(pixResponse);
        Billing billing = Billing.builder()
                .orderId(paymentRequest.orderId())
                .cafeteriaId(paymentRequest.cafeteriaId())
                .amount(paymentRequest.totalAmount())
                .clientName(paymentRequest.clientName())
                .status(BillingStatus.PENDING)
                .abacatePayId(pixResponse.id())
                .qrCode(pixResponse.brCode())
                .qrCodeImage(pixResponse.brCodeBase64())
                .expiredAt(Instant.parse(pixResponse.expiresAt()))
                .build();

        billingRepository.save(billing);
        return pixResponse;
    }

    @Transactional(noRollbackFor = BillingExpired.class)
    public void processPayment(AbacatePayWebhookPayload payload){
        Billing billing = billingRepository.findByAbacatePayId(payload.data().id()).orElseThrow(
                () -> new BillingNotFound(payload.data().id())
        );
        if(billing.getStatus().equals(BillingStatus.PENDING)){
            throw new BillingClosed(payload.data().id());
        }
        if(Instant.now().isAfter(billing.getExpiredAt())){
            billing.setStatus(BillingStatus.EXPIRED);
            billingRepository.save(billing);
            throw new BillingExpired(payload.data().id());
        }

        billing.setStatus(BillingStatus.PAID);
        billingRepository.save(billing);
        // chama o OrderService
    }

    @Transactional
    public Boolean isValidSignature(String rawPayload, String signature) {
        try {
            // 1. Prepara o algoritmo HMAC SHA-256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);

            // 2. Calcula o hash do payload cru recebido
            byte[] hash = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));

            // 3. Converte os bytes resultantes para uma String Hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String assinaturaCalculada = hexString.toString();

            // 4. Compara a assinatura calculada com a que veio no cabeçalho
            return MessageDigest.isEqual(
                    assinaturaCalculada.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            return false;
        }
    }
}

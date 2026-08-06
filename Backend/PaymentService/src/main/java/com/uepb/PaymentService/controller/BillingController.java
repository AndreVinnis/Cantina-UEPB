package com.uepb.PaymentService.controller;

import com.uepb.PaymentService.dto.request.PaymentRequest;
import com.uepb.PaymentService.integration.dto.response.PixTransparentResponseData;
import com.uepb.PaymentService.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @PostMapping("/public/create")
    public ResponseEntity<PixTransparentResponseData> create(@RequestBody @Valid PaymentRequest paymentRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createBillingPix(paymentRequest));
    }
}

package com.uepb.OrderService.controllers;

import com.uepb.OrderService.dto.request.OrderRequest;
import com.uepb.OrderService.dto.response.CafeteriaOrderResponse;
import com.uepb.OrderService.dto.response.ClientOrderResponse;
import com.uepb.OrderService.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/public/create")
    public ResponseEntity<ClientOrderResponse> create(@RequestBody @Valid OrderRequest orderRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(orderRequest));
    }

    @GetMapping("/merchant/open-orders")
    public ResponseEntity<List<CafeteriaOrderResponse>> getOpenOrders(){
        return ResponseEntity.ok().body(orderService.getOpenOrders());
    }

    @GetMapping("/merchant/all-orders")
    public ResponseEntity<List<CafeteriaOrderResponse>> getAllOrders(){
        return ResponseEntity.ok().body(orderService.getAllOrders());
    }
}

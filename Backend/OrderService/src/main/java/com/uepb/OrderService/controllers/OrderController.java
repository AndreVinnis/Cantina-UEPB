package com.uepb.OrderService.controllers;

import com.uepb.OrderService.dto.request.ChangeOrderRequest;
import com.uepb.OrderService.dto.request.CloseOrderRequest;
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

    @PatchMapping("/public/{id}/cancel-order")
    public ResponseEntity<ClientOrderResponse> cancelOrder(@PathVariable String id){
        return ResponseEntity.ok().body(orderService.cancelOrder(id));
    }

    @GetMapping("/public/{cpf}/orders")
    public ResponseEntity<List<ClientOrderResponse>> getOrderByCpf(@PathVariable String cpf){
        return ResponseEntity.ok().body(orderService.getOrderByCpf(cpf));
    }

    @GetMapping("/merchant/open-orders")
    public ResponseEntity<List<CafeteriaOrderResponse>> getOpenOrders(){
        return ResponseEntity.ok().body(orderService.getOpenOrders());
    }

    @GetMapping("/merchant/all-orders")
    public ResponseEntity<List<CafeteriaOrderResponse>> getAllOrders(){
        return ResponseEntity.ok().body(orderService.getAllOrders());
    }

    @PatchMapping("/merchant/change-status")
    public ResponseEntity<CafeteriaOrderResponse> changeOrderStatus(@RequestBody @Valid ChangeOrderRequest request){
        return ResponseEntity.ok().body(orderService.changeOrderStatus(request.orderId(), request.newStatus()));
    }

    @PatchMapping("/merchant/close-order")
    public ResponseEntity<CafeteriaOrderResponse> closeOrder(@RequestBody @Valid CloseOrderRequest request){
        return ResponseEntity.ok().body(orderService.closeOrder(request.orderId(), request.code()));
    }
}

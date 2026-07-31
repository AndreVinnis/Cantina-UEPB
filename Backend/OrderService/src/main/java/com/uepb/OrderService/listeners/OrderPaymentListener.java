package com.uepb.OrderService.listeners;

import com.uepb.OrderService.domain.Order;
import com.uepb.OrderService.domain.OrderItem;
import com.uepb.OrderService.dto.events.OrderCreatedEvent;
import com.uepb.OrderService.dto.request.OrderItemRequest;
import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import com.uepb.OrderService.exception.OrderNotFound;
import com.uepb.OrderService.integration.CoreService;
import com.uepb.OrderService.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderPaymentListener {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CoreService coreService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        if (event.paymentMethod() == PaymentMethod.PIX){
            Order order = orderRepository.findById(event.orderId()).orElseThrow(
                    () -> new OrderNotFound(event.orderId())
            );

            order.setStatus(Status.AWANTING_PAYMENT);
            orderRepository.save(order);
            //Lógica para chamar o PaymentService
        }
        else if(event.paymentMethod() == PaymentMethod.COUNTER){
            Order order = orderRepository.findById(event.orderId()).orElseThrow(
                    () -> new OrderNotFound(event.orderId())
            );
            List<OrderItemRequest> requests = new ArrayList<>();
            for(OrderItem orderItem: order.getItems()){
                requests.add(new OrderItemRequest(orderItem.getItemName(), orderItem.getQuantity()));
            }
            coreService.decrementsStock(order.getCafeteriaId(), requests);
        }
    }
}

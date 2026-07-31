package com.uepb.OrderService.service;

import com.uepb.OrderService.domain.Order;
import com.uepb.OrderService.domain.OrderItem;
import com.uepb.OrderService.dto.events.OrderCreatedEvent;
import com.uepb.OrderService.dto.request.OrderRequest;
import com.uepb.OrderService.dto.response.CafeteriaOrderResponse;
import com.uepb.OrderService.dto.response.ClientOrderResponse;
import com.uepb.OrderService.dto.response.OrderItemResponse;
import com.uepb.OrderService.enums.PaymentMethod;
import com.uepb.OrderService.enums.Status;
import com.uepb.OrderService.exception.InvalidStatus;
import com.uepb.OrderService.exception.NoOrdersYet;
import com.uepb.OrderService.exception.OrderNotFound;
import com.uepb.OrderService.exception.UserWithoutAccess;
import com.uepb.OrderService.integration.CoreService;
import com.uepb.OrderService.integration.ItemValidatedResponse;
import com.uepb.OrderService.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CoreService coreService;

    private final List<Status> openStatus = List.of(
            Status.PENDING,
            Status.CONFIRMED,
            Status.IN_PROGRESS,
            Status.READY
    );

    @Transactional
    public ClientOrderResponse create(OrderRequest orderRequest){
        List<ItemValidatedResponse> validatedItems = coreService.validateOrderItems(orderRequest.cafeteriaId(), orderRequest.items());
        Order order = new Order();
        order.setCafeteriaId(orderRequest.cafeteriaId());
        order.setCafeteriaName(orderRequest.cafeteriaName());
        order.setClientName(orderRequest.clientName());
        order.setPaymentMethod(orderRequest.paymentMethod());
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for(ItemValidatedResponse item: validatedItems){
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .itemId(item.id())
                    .itemName(item.name())
                    .quantity(item.quantity())
                    .uniquePrice(item.uniquePrice())
                    .build();
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getUniquePrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setTotalPrice(totalAmount);
        order.setItems(orderItems);
        order.setStatus(Status.PENDING);
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), order.getPaymentMethod()));
        return toClientOrderResponse(order);
    }

    @Transactional
    public List<CafeteriaOrderResponse> getOpenOrders(){
        String cafeteriaId = coreService.getIdCafeteria();
        List<Order> orders = orderRepository.findByCafeteriaId(cafeteriaId);
        if(orders.isEmpty()){
            throw new NoOrdersYet();
        }

        List<CafeteriaOrderResponse> orderResponses = new ArrayList<>();
        for (Order order: orders){
            if(openStatus.contains(order.getStatus())){
                orderResponses.add(toCafeteriaOrderResponse(order));
            }
        }
        return orderResponses;
    }

    @Transactional
    public List<CafeteriaOrderResponse> getAllOrders(){
        String cafeteriaId = coreService.getIdCafeteria();
        List<Order> orders = orderRepository.findByCafeteriaId(cafeteriaId);
        if(orders.isEmpty()){
            throw new NoOrdersYet();
        }

        List<CafeteriaOrderResponse> orderResponses = new ArrayList<>();
        for (Order order: orders){
            orderResponses.add(toCafeteriaOrderResponse(order));
        }
        return orderResponses;
    }

    @Transactional
    public CafeteriaOrderResponse changeOrderStatus(String id, Status newStatus){
        String cafeteriaId = coreService.getIdCafeteria();
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFound(id)
        );

        if(!order.getCafeteriaId().equals(cafeteriaId)){
            throw new UserWithoutAccess("Esse pedido não é dessa lanchonete.");
        }
        if(!openStatus.contains(order.getStatus())){
            throw new InvalidStatus(order.getStatus(), newStatus);
        }
        if(newStatus.equals(Status.COMPLETED)){
            throw new IllegalArgumentException("Caminho inválido para encerramento do pedido");
        }
        order.setStatus(newStatus);
        return toCafeteriaOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public CafeteriaOrderResponse closeOrder(String id, String code){
        String cafeteriaId = coreService.getIdCafeteria();
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFound(id)
        );

        if(!order.getCafeteriaId().equals(cafeteriaId)){
            throw new UserWithoutAccess("Esse pedido não é dessa lanchonete.");
        }
        if(order.getStatus().equals(Status.COMPLETED) || order.getStatus().equals(Status.CANCELLED)){
            throw new InvalidStatus(order.getStatus(), Status.COMPLETED);
        }
        if(order.getPaymentMethod().equals(PaymentMethod.PIX)){
            if(!order.getSessionToken().substring(order.getSessionToken().length() - 4).equals(code)){
                throw new IllegalArgumentException("Código de retirada incorreto");
            }
        }

        order.setStatus(Status.COMPLETED);
        return toCafeteriaOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public ClientOrderResponse cancelOrder(String id){
        List<Status> ableChangeStatus = List.of(
                Status.PENDING,
                Status.AWANTING_PAYMENT,
                Status.CONFIRMED
        );
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new OrderNotFound(id)
        );

        if(!ableChangeStatus.contains(order.getStatus())){
            throw new InvalidStatus(order.getStatus(), Status.CANCELLED);
        }
        order.setStatus(Status.CANCELLED);
        return toClientOrderResponse(orderRepository.save(order));
    }

    private ClientOrderResponse toClientOrderResponse(Order order){
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for(OrderItem item: order.getItems()){
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUniquePrice(),
                    item.getUniquePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            itemResponses.add(orderItemResponse);
        }
        return new ClientOrderResponse(
                order.getSessionToken(),
                order.getCafeteriaName(),
                itemResponses,
                order.getTotalPrice(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    private CafeteriaOrderResponse toCafeteriaOrderResponse(Order order){
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for(OrderItem item: order.getItems()){
            OrderItemResponse orderItemResponse = new OrderItemResponse(
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUniquePrice(),
                    item.getUniquePrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
            itemResponses.add(orderItemResponse);
        }
        return new CafeteriaOrderResponse(
                order.getId(),
                order.getClientName(),
                itemResponses,
                order.getTotalPrice(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}

package com.flash.order.controller;

import com.flash.order.domain.Order;
import com.flash.order.dto.OrderResponse;
import com.flash.order.service.OrderService;
import com.flash.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping("/purchase")
    public ResponseEntity<OrderResponse> purchaseItem(
        @RequestParam Long itemId, 
        @RequestParam Long userId
    ) {
        Order order = orderService.createOrder(userId, itemId);  // user -> userId로 변경
        return ResponseEntity.ok(OrderResponse.from(order));
    }
    
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(
        @RequestParam Long userId
    ) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
} 
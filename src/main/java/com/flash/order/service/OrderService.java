package com.flash.order.service;

import com.flash.order.domain.Order;
import com.flash.order.dto.OrderResponse;
import com.flash.user.domain.User;
import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, Long itemId, Integer quantity);
    List<OrderResponse> getOrdersByUserId(Long userId);
    Order getOrder(Long orderId);
} 
package com.flash.order.dto;

import com.flash.order.domain.Order;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class OrderResponse {
    private final Long id;
    private final String itemName;
    private final Integer quantity;
    private final Integer totalAmount;
    private final String status;
    private final LocalDateTime createdAt;
    
    private OrderResponse(Long id, String itemName, Integer quantity, 
                         Integer totalAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getItem().getName(),
            order.getQuantity(),
            order.getItem().getPrice() * order.getQuantity(),
            order.getStatus().name(),
            order.getCreatedAt()
        );
    }
} 
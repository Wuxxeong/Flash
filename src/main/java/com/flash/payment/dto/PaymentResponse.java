package com.flash.payment.dto;

import com.flash.payment.domain.Payment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PaymentResponse {
    private final Long id;
    private final Long orderId;
    private final Integer amount;
    private final String status;
    private final LocalDateTime createdAt;
    
    private PaymentResponse(Long id, Long orderId, Integer amount, 
                           String status, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrder().getId(),
            payment.getAmount(),
            payment.getStatus().name(),
            payment.getCreatedAt()
        );
    }
} 
package com.flash.payment.controller;

import com.flash.order.domain.Order;
import com.flash.order.service.OrderService;
import com.flash.payment.domain.Payment;
import com.flash.payment.dto.PaymentResponse;
import com.flash.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestParam Long orderId) {
        Order order = orderService.getOrder(orderId);
        Payment payment = paymentService.createPayment(order);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestParam Long paymentId) {
        Payment payment = paymentService.processPayment(paymentId);
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
} 
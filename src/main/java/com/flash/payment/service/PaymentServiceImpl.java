package com.flash.payment.service;

import com.flash.order.domain.Order;
import com.flash.order.exception.OrderException;
import com.flash.payment.domain.Payment;
import com.flash.payment.exception.PaymentException;
import com.flash.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    @Override
    public Payment createPayment(Order order) {
        // 주문 상태 확인
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new OrderException.InvalidOrderStatusException();
        }
        
        // 결제 생성
        Payment payment = Payment.builder()
            .order(order)
            .amount(order.getTotalAmount())
            .build();
            
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(PaymentException.PaymentNotFoundException::new);
            
        // 결제 상태 확인
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new PaymentException.InvalidPaymentStatusException();
        }
        
        // 결제 처리
        try {
            payment.complete();
            payment.getOrder().complete();
        } catch (Exception e) {
            throw new PaymentException.PaymentProcessFailedException();
        }
        
        return payment;
    }
} 
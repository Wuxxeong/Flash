package com.flash.payment.service;

import com.flash.payment.domain.Payment;
import com.flash.order.domain.Order;

public interface PaymentService {
    Payment createPayment(Order order);
    Payment processPayment(Long paymentId);
} 
package com.flash.payment.repository;

import com.flash.payment.domain.Payment;
import com.flash.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrder(Order order);
}
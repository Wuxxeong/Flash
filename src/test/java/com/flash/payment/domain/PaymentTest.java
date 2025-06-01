package com.flash.payment.domain;

import com.flash.item.domain.Item;
import com.flash.order.domain.Order;
import com.flash.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    @DisplayName("결제 생성 테스트")
    void createPayment() {
        // given
        Order order = Order.builder()
            .user(User.builder().build())
            .item(Item.builder().build())
            .quantity(1)
            .build();

        // when
        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        // then
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getAmount()).isEqualTo(10000);
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getPaidAt()).isNull();
    }

    @Test
    @DisplayName("결제 상태 변경 테스트")
    void updatePaymentStatus() {
        // given
        Payment payment = Payment.builder()
            .order(Order.builder().build())
            .amount(10000)
            .build();

        // when
        payment.updateStatus(Payment.PaymentStatus.COMPLETED);

        // then
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("결제 완료 테스트")
    void completePayment() {
        // given
        Payment payment = Payment.builder()
            .order(Order.builder().build())
            .amount(10000)
            .build();

        // when
        payment.complete();

        // then
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(payment.getPaidAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("결제 키 설정 테스트")
    void setPaymentKey() {
        // given
        Payment payment = Payment.builder()
            .order(Order.builder().build())
            .amount(10000)
            .build();
        String paymentKey = "test_payment_key";

        // when
        payment.setPaymentKey(paymentKey);

        // then
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("결제 생성 시 기본 상태는 PENDING")
    void defaultPaymentStatusIsPending() {
        // given
        Payment payment = Payment.builder()
            .order(Order.builder().build())
            .amount(10000)
            .build();

        // then
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제 생성 시 생성 시간이 설정됨")
    void paymentHasCreationTime() {
        // given
        Payment payment = Payment.builder()
            .order(Order.builder().build())
            .amount(10000)
            .build();

        // then
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
} 
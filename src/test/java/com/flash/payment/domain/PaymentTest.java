package com.flash.payment.domain;

import com.flash.item.domain.Item;
import com.flash.order.domain.Order;
import com.flash.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private User createTestUser() {
        return User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();
    }

    private Item createTestItem() {
        return Item.builder()
            .name("Test Item")
            .description("Test Description")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();
    }

    @Test
    @DisplayName("결제 생성 테스트")
    void createPayment() {
        // given
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
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
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
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
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        // when
        payment.complete();

        // then
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 키 설정 테스트")
    void setPaymentKey() {
        // given
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        String paymentKey = "test_payment_key";

        // when
        payment.setPaymentKey(paymentKey);

        // then
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("기본 결제 상태는 PENDING")
    void defaultPaymentStatusIsPending() {
        // given
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        // then
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제 생성 시 생성 시간이 설정됨")
    void paymentHasCreationTime() {
        // given
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        // then
        assertThat(payment.getCreatedAt()).isNotNull();
        assertThat(payment.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
} 
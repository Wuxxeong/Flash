package com.flash.payment.service;

import com.flash.item.domain.Item;
import com.flash.order.domain.Order;
import com.flash.order.exception.OrderException;
import com.flash.payment.domain.Payment;
import com.flash.payment.exception.PaymentException;
import com.flash.payment.repository.PaymentRepository;
import com.flash.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

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

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // when
        Payment createdPayment = paymentService.createPayment(order);

        // then
        assertThat(createdPayment.getOrder()).isEqualTo(order);
        assertThat(createdPayment.getAmount()).isEqualTo(10000);
        assertThat(createdPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("잘못된 주문 상태로 결제 생성 시도 시 예외 발생")
    void createPaymentWithInvalidOrderStatus() {
        // given
        Order order = Order.builder()
            .user(createTestUser())
            .item(createTestItem())
            .quantity(1)
            .build();
        order.updateStatus(Order.OrderStatus.PAID);

        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(order))
            .isInstanceOf(OrderException.InvalidOrderStatusException.class);
    }

    @Test
    @DisplayName("결제 처리 테스트")
    void processPayment() {
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

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // when
        Payment processedPayment = paymentService.processPayment(1L);

        // then
        assertThat(processedPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(processedPayment.getOrder().getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    @Test
    @DisplayName("존재하지 않는 결제 처리 시도 시 예외 발생")
    void processPaymentNotFound() {
        // given
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(1L))
            .isInstanceOf(PaymentException.PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("이미 완료된 결제 처리 시도 시 예외 발생")
    void processPaymentAlreadyCompleted() {
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
        payment.complete();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(1L))
            .isInstanceOf(PaymentException.InvalidPaymentStatusException.class);
    }
} 
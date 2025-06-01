package com.flash.order.domain;

import com.flash.item.domain.Item;
import com.flash.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("주문 생성 테스트")
    void createOrder() {
        // given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        Item item = Item.builder()
            .name("Test Item")
            .description("Test Description")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now().minusDays(1))
            .saleEnd(LocalDateTime.now().plusDays(1))
            .build();

        // when
        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(2)
            .build();

        // then
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getItem()).isEqualTo(item);
        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 상태 변경 테스트")
    void updateOrderStatus() {
        // given
        Order order = Order.builder()
            .user(User.builder().build())
            .item(Item.builder().build())
            .quantity(1)
            .build();

        // when
        order.updateStatus(Order.OrderStatus.PAID);

        // then
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    @Test
    @DisplayName("주문 총 금액 계산 테스트")
    void calculateTotalAmount() {
        // given
        Item item = Item.builder()
            .name("Test Item")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now().minusDays(1))
            .saleEnd(LocalDateTime.now().plusDays(1))
            .build();

        Order order = Order.builder()
            .user(User.builder().build())
            .item(item)
            .quantity(3)
            .build();

        // when
        int totalAmount = order.getTotalAmount();

        // then
        assertThat(totalAmount).isEqualTo(30000); // 10000 * 3
    }

    @Test
    @DisplayName("주문 완료 테스트")
    void completeOrder() {
        // given
        Order order = Order.builder()
            .user(User.builder().build())
            .item(Item.builder().build())
            .quantity(1)
            .build();

        // when
        order.complete();

        // then
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    @Test
    @DisplayName("주문 생성 시 기본 상태는 PENDING")
    void defaultOrderStatusIsPending() {
        // given
        Order order = Order.builder()
            .user(User.builder().build())
            .item(Item.builder().build())
            .quantity(1)
            .build();

        // then
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 생성 시 생성 시간이 설정됨")
    void orderHasCreationTime() {
        // given
        Order order = Order.builder()
            .user(User.builder().build())
            .item(Item.builder().build())
            .quantity(1)
            .build();

        // then
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
} 
package com.flash.order.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderExceptionTest {

    @Test
    @DisplayName("OrderNotFoundException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createOrderNotFoundException() {
        // when
        OrderException.OrderNotFoundException exception = 
            new OrderException.OrderNotFoundException();

        // then
        assertThat(exception.getMessage()).isEqualTo("주문을 찾을 수 없습니다.");
        assertThat(exception.getCode()).isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    @DisplayName("DuplicateOrderException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createDuplicateOrderException() {
        // when
        OrderException.DuplicateOrderException exception = 
            new OrderException.DuplicateOrderException();

        // then
        assertThat(exception.getMessage()).isEqualTo("이미 구매한 상품입니다.");
        assertThat(exception.getCode()).isEqualTo("DUPLICATE_ORDER");
    }

    @Test
    @DisplayName("InvalidOrderStatusException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createInvalidOrderStatusException() {
        // when
        OrderException.InvalidOrderStatusException exception = 
            new OrderException.InvalidOrderStatusException();

        // then
        assertThat(exception.getMessage()).isEqualTo("잘못된 주문 상태입니다.");
        assertThat(exception.getCode()).isEqualTo("INVALID_ORDER_STATUS");
    }
} 
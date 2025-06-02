package com.flash.order.exception;

import com.flash.common.exception.BaseException;

public class OrderException {
    public static class OrderNotFoundException extends BaseException {
        public OrderNotFoundException() {
            super("주문을 찾을 수 없습니다.", "ORDER_NOT_FOUND");
        }
    }
    
    public static class DuplicateOrderException extends BaseException {
        public DuplicateOrderException() {
            super("이미 구매한 상품입니다.", "DUPLICATE_ORDER");
        }
    }
    
    public static class InvalidOrderStatusException extends BaseException {
        public InvalidOrderStatusException() {
            super("잘못된 주문 상태입니다.", "INVALID_ORDER_STATUS");
        }
    }

    public OrderException() {
    }
} 
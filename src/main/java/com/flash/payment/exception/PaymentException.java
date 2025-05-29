package com.flash.payment.exception;

import com.flash.common.exception.BaseException;

public class PaymentException {
    public static class PaymentNotFoundException extends BaseException {
        public PaymentNotFoundException() {
            super("결제 정보를 찾을 수 없습니다.", "PAYMENT_NOT_FOUND");
        }
    }
    
    public static class InvalidPaymentStatusException extends BaseException {
        public InvalidPaymentStatusException() {
            super("잘못된 결제 상태입니다.", "INVALID_PAYMENT_STATUS");
        }
    }
    
    public static class PaymentProcessFailedException extends BaseException {
        public PaymentProcessFailedException() {
            super("결제 처리에 실패했습니다.", "PAYMENT_PROCESS_FAILED");
        }
    }
} 
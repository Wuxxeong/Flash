package com.flash.payment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentExceptionTest {

    @Test
    @DisplayName("PaymentNotFoundException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createPaymentNotFoundException() {
        // when
        PaymentException.PaymentNotFoundException exception = 
            new PaymentException.PaymentNotFoundException();

        // then
        assertThat(exception.getMessage()).isEqualTo("결제 정보를 찾을 수 없습니다.");
        assertThat(exception.getCode()).isEqualTo("PAYMENT_NOT_FOUND");
    }

    @Test
    @DisplayName("InvalidPaymentStatusException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createInvalidPaymentStatusException() {
        // when
        PaymentException.InvalidPaymentStatusException exception = 
            new PaymentException.InvalidPaymentStatusException();

        // then
        assertThat(exception.getMessage()).isEqualTo("잘못된 결제 상태입니다.");
        assertThat(exception.getCode()).isEqualTo("INVALID_PAYMENT_STATUS");
    }

    @Test
    @DisplayName("PaymentProcessFailedException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createPaymentProcessFailedException() {
        // when
        PaymentException.PaymentProcessFailedException exception = 
            new PaymentException.PaymentProcessFailedException();

        // then
        assertThat(exception.getMessage()).isEqualTo("결제 처리에 실패했습니다.");
        assertThat(exception.getCode()).isEqualTo("PAYMENT_PROCESS_FAILED");
    }
} 
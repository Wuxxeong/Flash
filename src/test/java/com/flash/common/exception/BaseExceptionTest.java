package com.flash.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseExceptionTest {

    @Test
    @DisplayName("BaseException 생성 시 메시지와 코드가 올바르게 설정되는지 확인")
    void createBaseException() {
        // given
        String message = "테스트 메시지";
        String code = "TEST_CODE";

        // when
        BaseException exception = new BaseException(message, code);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCode()).isEqualTo(code);
    }
}
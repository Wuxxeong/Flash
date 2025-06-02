package com.flash.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BaseException 처리 시 올바른 응답이 반환되는지 확인")
    void handleBaseException() {
        // given
        String message = "테스트 메시지";
        String code = "TEST_CODE";
        BaseException exception = new BaseException(message, code);

        // when
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleBaseException(exception);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(code);
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }
} 
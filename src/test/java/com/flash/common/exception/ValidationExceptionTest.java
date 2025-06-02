package com.flash.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ValidationExceptionTest {

    @Test
    @DisplayName("InvalidEmailException 생성 시 올바른 메시지가 설정되는지 확인")
    void createInvalidEmailException() {
        // when
        ValidationException.InvalidEmailException exception = 
            new ValidationException.InvalidEmailException();

        // then
        assertThat(exception.getMessage()).isEqualTo("이메일 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("InvalidPasswordException 생성 시 올바른 메시지가 설정되는지 확인")
    void createInvalidPasswordException() {
        // when
        ValidationException.InvalidPasswordException exception = 
            new ValidationException.InvalidPasswordException();

        // then
        assertThat(exception.getMessage()).isEqualTo("비밀번호는 8자 이상이어야 합니다");
    }

    @Test
    @DisplayName("EmptyNameException 생성 시 올바른 메시지가 설정되는지 확인")
    void createEmptyNameException() {
        // when
        ValidationException.EmptyNameException exception = 
            new ValidationException.EmptyNameException();

        // then
        assertThat(exception.getMessage()).isEqualTo("이름은 비어있을 수 없습니다");
    }
} 
package com.flash.user.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionTest {

    @Test
    @DisplayName("UserNotFoundException 생성 시 올바른 메시지가 설정되는지 확인")
    void createUserNotFoundException() {
        // when
        UserException.UserNotFoundException exception = 
            new UserException.UserNotFoundException();

        // then
        assertThat(exception.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("UserAlreadyExistsException 생성 시 올바른 메시지가 설정되는지 확인")
    void createUserAlreadyExistsException() {
        // when
        UserException.UserAlreadyExistsException exception = 
            new UserException.UserAlreadyExistsException();

        // then
        assertThat(exception.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("InvalidPasswordException 생성 시 올바른 메시지가 설정되는지 확인")
    void createInvalidPasswordException() {
        // when
        UserException.InvalidPasswordException exception = 
            new UserException.InvalidPasswordException();

        // then
        assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("UnauthorizedException 생성 시 올바른 메시지가 설정되는지 확인")
    void createUnauthorizedException() {
        // when
        UserException.UnauthorizedException exception = 
            new UserException.UnauthorizedException();

        // then
        assertThat(exception.getMessage()).isEqualTo("Unauthorized access");
    }

    @Test
    @DisplayName("UserException 생성 시 메시지와 원인이 올바르게 설정되는지 확인")
    void createUserExceptionWithMessageAndCause() {
        // given
        String message = "테스트 메시지";
        Throwable cause = new RuntimeException("원인");

        // when
        UserException exception = new UserException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }
} 
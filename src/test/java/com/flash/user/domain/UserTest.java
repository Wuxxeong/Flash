package com.flash.user.domain;

import com.flash.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private User createTestUser() {
        return User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();
    }

    @Test
    @DisplayName("사용자 생성 테스트")
    void createUser() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";

        // when
        User user = User.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않은 경우 예외 발생")
    void invalidEmailFormat() {
        // given
        String invalidEmail = "invalid-email";
        String password = "password123";
        String name = "Test User";

        // when & then
        assertThatThrownBy(() -> User.builder()
            .email(invalidEmail)
            .password(password)
            .name(name)
            .build())
            .isInstanceOf(ValidationException.InvalidEmailException.class);
    }

    @Test
    @DisplayName("비밀번호가 8자 미만인 경우 예외 발생")
    void invalidPasswordLength() {
        // given
        String email = "test@example.com";
        String shortPassword = "pass";
        String name = "Test User";

        // when & then
        assertThatThrownBy(() -> User.builder()
            .email(email)
            .password(shortPassword)
            .name(name)
            .build())
            .isInstanceOf(ValidationException.InvalidPasswordException.class);
    }

    @Test
    @DisplayName("이름이 비어있는 경우 예외 발생")
    void emptyName() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String emptyName = "";

        // when & then
        assertThatThrownBy(() -> User.builder()
            .email(email)
            .password(password)
            .name(emptyName)
            .build())
            .isInstanceOf(ValidationException.EmptyNameException.class);
    }

    @Test
    @DisplayName("사용자 생성 시 생성 시간이 설정됨")
    void userHasCreationTime() {
        // given
        User user = createTestUser();

        // then
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일이 null인 경우 예외 발생")
    void nullEmail() {
        String password = "password123";
        String name = "Test User";
        assertThatThrownBy(() -> User.builder()
            .email(null)
            .password(password)
            .name(name)
            .build())
            .isInstanceOf(ValidationException.InvalidEmailException.class);
    }

    @Test
    @DisplayName("비밀번호가 null인 경우 예외 발생")
    void nullPassword() {
        String email = "test@example.com";
        String name = "Test User";
        assertThatThrownBy(() -> User.builder()
            .email(email)
            .password(null)
            .name(name)
            .build())
            .isInstanceOf(ValidationException.InvalidPasswordException.class);
    }

    @Test
    @DisplayName("이름이 null인 경우 예외 발생")
    void nullName() {
        String email = "test@example.com";
        String password = "password123";
        assertThatThrownBy(() -> User.builder()
            .email(email)
            .password(password)
            .name(null)
            .build())
            .isInstanceOf(ValidationException.EmptyNameException.class);
    }
} 
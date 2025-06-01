package com.flash.user.service;

import com.flash.user.domain.User;
import com.flash.user.exception.UserException;
import com.flash.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void getUserByEmail() {
        // given
        String email = "test@example.com";
        User user = User.builder()
            .email(email)
            .password("password123")
            .name("Test User")
            .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        User foundUser = userService.getUserByEmail(email);

        // then
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 시 예외 발생")
    void getUserByEmailNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
            .isInstanceOf(UserException.UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 생성 테스트")
    void createUser() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";

        User expectedUser = User.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // when
        User createdUser = userService.createUser(email, password, name);
        
        // then
        assertThat(createdUser.getEmail()).isEqualTo(email);
        assertThat(createdUser.getName()).isEqualTo(name);
        assertThat(createdUser.getPassword()).isEqualTo(password);
    }
} 
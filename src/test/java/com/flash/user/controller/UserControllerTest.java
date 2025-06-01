package com.flash.user.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.user.domain.User;
import com.flash.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("이메일로 사용자 조회 API 테스트")
    void getUserByEmail() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";

        User user = User.builder()
            .email(email)
            .password(password)
            .name(name)
            .build();

        when(userService.createUser(email, password, name)).thenReturn(user);

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .param("email", email)
                .param("password", password)
                .param("name", name))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is(email)))
            .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    @DisplayName("사용자 정보 조회 API 테스트")
    void getUserInfo() throws Exception {
        // given
        String email = "test@example.com";
        User user = User.builder()
            .email(email)
            .password("password123")
            .name("Test User")
            .build();

        when(userService.getUserByEmail(anyString())).thenReturn(user);

        // when & then
        mockMvc.perform(get("/api/users/me")
                .param("email", email))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email", is(email)))
            .andExpect(jsonPath("$.name", is("Test User")));
    }
} 
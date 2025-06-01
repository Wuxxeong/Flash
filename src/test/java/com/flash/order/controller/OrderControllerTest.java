package com.flash.order.controller;

import com.flash.item.domain.Item;
import com.flash.order.domain.Order;
import com.flash.order.dto.OrderResponse;
import com.flash.order.service.OrderService;
import com.flash.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 API 테스트")
    void createOrder() throws Exception {
        // given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        Item item = Item.builder()
            .name("Test Item")
            .description("Test Description")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now().minusDays(1))
            .saleEnd(LocalDateTime.now().plusDays(1))
            .build();

        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(2)
            .build();

        when(orderService.createOrder(anyLong(), anyLong(), anyInt())).thenReturn(order);

        // when & then
        mockMvc.perform(post("/api/purchase")
                .param("userId", "1")
                .param("itemId", "1")
                .param("quantity", "2"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자의 주문 목록 조회 API 테스트")
    void getUserOrders() throws Exception {
        // given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        Order order1 = Order.builder()
            .user(user)
            .item(Item.builder()
                .name("Item 1")
                .price(10000)
                .stock(10)
                .saleStart(LocalDateTime.now().minusDays(1))
                .saleEnd(LocalDateTime.now().plusDays(1))
                .build())
            .quantity(1)
            .build();

        Order order2 = Order.builder()
            .user(user)
            .item(Item.builder()
                .name("Item 2")
                .price(20000)
                .stock(5)
                .saleStart(LocalDateTime.now().minusDays(1))
                .saleEnd(LocalDateTime.now().plusDays(1))
                .build())
            .quantity(2)
            .build();

        List<OrderResponse> orders = Arrays.asList(
            OrderResponse.from(order1),
            OrderResponse.from(order2)
        );
        when(orderService.getOrdersByUserId(anyLong())).thenReturn(orders);

        // when & then
        mockMvc.perform(get("/api/orders")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].itemName", is("Item 1")))
            .andExpect(jsonPath("$[1].itemName", is("Item 2")));
    }
} 
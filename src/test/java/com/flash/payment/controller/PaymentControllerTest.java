package com.flash.payment.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flash.item.domain.Item;
import com.flash.order.domain.Order;
import com.flash.order.service.OrderService;
import com.flash.payment.domain.Payment;
import com.flash.payment.service.PaymentService;
import com.flash.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("결제 생성 API 테스트")
    void createPayment() throws Exception {
        // given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        Item item = Item.builder()
            .name("Test Item")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now().minusDays(1))
            .saleEnd(LocalDateTime.now().plusDays(1))
            .build();

        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();

        when(orderService.getOrder(anyLong())).thenReturn(order);
        when(paymentService.createPayment(any(Order.class))).thenReturn(payment);

        // when & then
        mockMvc.perform(post("/api/payment")
                .param("orderId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount", is(10000)))
            .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("결제 처리 API 테스트")
    void processPayment() throws Exception {
        // given
        User user = User.builder()
            .email("test@example.com")
            .password("password123")
            .name("Test User")
            .build();

        Item item = Item.builder()
            .name("Test Item")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now().minusDays(1))
            .saleEnd(LocalDateTime.now().plusDays(1))
            .build();

        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(1)
            .build();

        Payment payment = Payment.builder()
            .order(order)
            .amount(10000)
            .build();
        payment.complete();

        when(paymentService.processPayment(anyLong())).thenReturn(payment);

        // when & then
        mockMvc.perform(post("/api/payment/process")
                .param("paymentId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amount", is(10000)))
            .andExpect(jsonPath("$.status", is("COMPLETED")));
    }
} 
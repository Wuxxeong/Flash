package com.flash.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flash.item.domain.Item;
import com.flash.item.repository.ItemRepository;
import com.flash.item.service.ItemService;
import com.flash.order.domain.Order;
import com.flash.order.dto.OrderResponse;
import com.flash.order.repository.OrderRepository;
import com.flash.user.domain.User;
import com.flash.user.repository.UserRepository;
import com.flash.user.service.UserService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("주문 생성 테스트")
    void createOrder() {
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        Order createdOrder = orderService.createOrder(1L, 1L, 2);

        // then
        assertThat(createdOrder.getUser()).isEqualTo(user);
        assertThat(createdOrder.getItem()).isEqualTo(item);
        assertThat(createdOrder.getQuantity()).isEqualTo(2);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("사용자의 모든 주문 조회 테스트")
    void getUserOrders() {
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

        List<Order> orders = Arrays.asList(order1, order2);
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // when
        List<OrderResponse> foundOrders = orderService.getOrdersByUserId(1L);

        // then
        assertThat(foundOrders).hasSize(2);
        assertThat(foundOrders.get(0).getItemName()).isEqualTo("Item 1");
        assertThat(foundOrders.get(1).getItemName()).isEqualTo("Item 2");
    }

} 
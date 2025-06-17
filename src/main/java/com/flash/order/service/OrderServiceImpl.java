package com.flash.order.service;

import com.flash.item.domain.Item;
import com.flash.item.exception.ItemException;
import com.flash.item.repository.ItemRepository;
import com.flash.order.domain.Order;
import com.flash.order.domain.Order.OrderStatus;
import com.flash.order.dto.OrderResponse;
import com.flash.order.exception.OrderException;
import com.flash.order.repository.OrderRepository;
import com.flash.user.domain.User;
import com.flash.user.exception.UserException;
import com.flash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    
    @Override
    @Transactional
    public Order createOrder(Long userId, Long itemId, Integer quantity) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(UserException.UserNotFoundException::new);
            
        // 상품 조회
        Item item = itemRepository.findById(itemId)
            .orElseThrow(ItemException.ItemNotFoundException::new);
            
        // 재고 확인
        if (item.getStock() < quantity) {
            throw new ItemException.OutOfStockException();
        }

        // 주문 생성
        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(quantity)
            .build();
            
        order = orderRepository.save(order);
        
        // 재고 차감
        item.decreaseStock(quantity);
        itemRepository.save(item);
        
        return order;
    }
    
    @Override
    @Transactional
    public Order createOrderV2(Long userId, Long itemId, Integer quantity) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(UserException.UserNotFoundException::new);
            
        // 상품 조회
        Item item = itemRepository.findById(itemId)
            .orElseThrow(ItemException.ItemNotFoundException::new);
            
        // 주문 생성
        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(quantity)
            .build();
            
        order = orderRepository.save(order);
        
        // AtomicInteger를 사용한 재고 차감
        item.decreaseStockV2(quantity);
        itemRepository.save(item);
        
        return order;
    }
    
    @Override
    @Transactional
    public Order createOrderV3(Long userId, Long itemId, Integer quantity) {
        // 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(UserException.UserNotFoundException::new);
            
        // Pessimistic Lock을 사용하여 상품 조회
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
            .orElseThrow(ItemException.ItemNotFoundException::new);
            
        // 주문 생성
        Order order = Order.builder()
            .user(user)
            .item(item)
            .quantity(quantity)
            .build();
            
        order = orderRepository.save(order);
        
        // 재고 차감
        item.decreaseStockV3(quantity);
        itemRepository.save(item);
        
        return order;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
            .map(OrderResponse::from)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(OrderException.OrderNotFoundException::new);
    }
} 
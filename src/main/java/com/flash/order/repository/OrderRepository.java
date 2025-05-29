package com.flash.order.repository;

import com.flash.order.domain.Order;
import com.flash.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    boolean existsByUserAndItemId(User user, Long itemId);
} 
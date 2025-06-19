package com.flash.order.domain;

import com.flash.item.domain.Item;
import com.flash.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public Order(User user, Item item, Integer quantity) {
        this.user = user;
        this.item = item;
        this.quantity = quantity;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
    
    public int getTotalAmount() {
        return item.getPrice() * quantity;
    }
    
    public void complete() {
        this.status = OrderStatus.PAID;
    }
    
    public enum OrderStatus {
        PENDING,    // 결제 대기
        PAID,       // 결제 완료
        FAILED      // 결제 실패
    }
} 
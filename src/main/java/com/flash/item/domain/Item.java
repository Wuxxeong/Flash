package com.flash.item.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sale_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer price;
    
    @Column(nullable = false)
    private Integer stock;
    
    @Column(name = "sale_start", nullable = false)
    private LocalDateTime saleStart;
    
    @Column(name = "sale_end", nullable = false)
    private LocalDateTime saleEnd;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public Item(String name, String description, Integer price, Integer stock, 
                LocalDateTime saleStart, LocalDateTime saleEnd) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.saleStart = saleStart;
        this.saleEnd = saleEnd;
        this.createdAt = LocalDateTime.now();
    }
    
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
    
    public boolean isOnSale() {
        LocalDateTime now = LocalDateTime.now();
        return (now.isEqual(saleStart) || now.isAfter(saleStart)) && now.isBefore(saleEnd);
    }
} 
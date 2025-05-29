package com.flash.item.dto;

import com.flash.item.domain.Item;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ItemResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final Integer price;
    private final Integer stock;
    private final LocalDateTime saleStart;
    private final LocalDateTime saleEnd;
    
    private ItemResponse(Long id, String name, String description, Integer price, 
                        Integer stock, LocalDateTime saleStart, LocalDateTime saleEnd) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.saleStart = saleStart;
        this.saleEnd = saleEnd;
    }
    
    public static ItemResponse from(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getPrice(),
            item.getStock(),
            item.getSaleStart(),
            item.getSaleEnd()
        );
    }
} 
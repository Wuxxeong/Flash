package com.flash.item.controller;

import com.flash.item.domain.Item;
import com.flash.item.dto.ItemResponse;
import com.flash.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems() {

        List<ItemResponse> items = itemService.getAllOnSaleItems().stream()
            .map(ItemResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        Item item = itemService.getOnSaleItemById(id);
        return ResponseEntity.ok(ItemResponse.from(item));
    }
} 
package com.flash.item.service;

import com.flash.item.domain.Item;
import com.flash.item.exception.ItemException;
import com.flash.item.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Item> getAllOnSaleItems() {
        return itemRepository.findAllOnSale(LocalDateTime.now());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Item getOnSaleItemById(Long id) {
        return itemRepository.findOnSaleById(id)
            .orElseThrow(ItemException.ItemNotFoundException::new);
    }
} 
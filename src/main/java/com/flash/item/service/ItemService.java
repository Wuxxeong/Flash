package com.flash.item.service;

import com.flash.item.domain.Item;
import java.util.List;

public interface ItemService {
    List<Item> getAllOnSaleItems();
    Item getOnSaleItemById(Long id);
} 
package com.flash.item.controller;

import com.flash.item.domain.Item;
import com.flash.item.dto.ItemResponse;
import com.flash.item.service.ItemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("판매 중인 모든 상품 조회 API 테스트")
    void getItems() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        Item item1 = Item.builder()
            .name("상품1")
            .description("설명1")
            .price(10000)
            .stock(10)
            .saleStart(now.minusDays(1))
            .saleEnd(now.plusDays(1))
            .build();

        Item item2 = Item.builder()
            .name("상품2")
            .description("설명2")
            .price(20000)
            .stock(5)
            .saleStart(now.minusDays(1))
            .saleEnd(now.plusDays(1))
            .build();

        List<Item> items = Arrays.asList(item1, item2);
        when(itemService.getAllOnSaleItems()).thenReturn(items);

        // when & then
        mockMvc.perform(get("/api/items"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("상품1")))
            .andExpect(jsonPath("$[1].name", is("상품2")));

        verify(itemService, times(1)).getAllOnSaleItems();
    }

    @Test
    @DisplayName("판매 중인 특정 상품 조회 API 테스트")
    void getItem() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now.minusDays(1))
            .saleEnd(now.plusDays(1))
            .build();

        when(itemService.getOnSaleItemById(1L)).thenReturn(item);

        // when & then
        mockMvc.perform(get("/api/items/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("테스트 상품")))
            .andExpect(jsonPath("$.description", is("테스트 설명")))
            .andExpect(jsonPath("$.price", is(10000)))
            .andExpect(jsonPath("$.stock", is(10)));

        verify(itemService, times(1)).getOnSaleItemById(1L);
    }
} 
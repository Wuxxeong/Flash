package com.flash.item.service;

import com.flash.item.domain.Item;
import com.flash.item.exception.ItemException;
import com.flash.item.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Test
    @DisplayName("판매 중인 모든 상품 조회 테스트")
    void getAllOnSaleItems() {
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

        when(itemRepository.findAllOnSale(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(item1, item2));

        // when
        List<Item> items = itemService.getAllOnSaleItems();

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("상품1");
        assertThat(items.get(1).getName()).isEqualTo("상품2");
        verify(itemRepository, times(1)).findAllOnSale(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("판매 중인 특정 상품 조회 테스트")
    void getOnSaleItemById() {
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

        when(itemRepository.findOnSaleById(1L))
            .thenReturn(Optional.of(item));

        // when
        Item foundItem = itemService.getOnSaleItemById(1L);

        // then
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getName()).isEqualTo("테스트 상품");
        verify(itemRepository, times(1)).findOnSaleById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생 테스트")
    void getOnSaleItemByIdNotFound() {
        // given
        when(itemRepository.findOnSaleById(1L))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.getOnSaleItemById(1L))
            .isInstanceOf(ItemException.ItemNotFoundException.class);
        verify(itemRepository, times(1)).findOnSaleById(1L);
    }
} 
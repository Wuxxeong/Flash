package com.flash.item.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class ItemTest {

    @Test
    @DisplayName("상품 생성 테스트")
    void createItem() {
        // given
        String name = "테스트 상품";
        String description = "테스트 설명";
        Integer price = 10000;
        Integer stock = 10;
        LocalDateTime saleStart = LocalDateTime.now();
        LocalDateTime saleEnd = saleStart.plusDays(7);

        // when
        Item item = Item.builder()
            .name(name)
            .description(description)
            .price(price)
            .stock(stock)
            .saleStart(saleStart)
            .saleEnd(saleEnd)
            .build();

        // then
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getDescription()).isEqualTo(description);
        assertThat(item.getPrice()).isEqualTo(price);
        assertThat(item.getStock()).isEqualTo(stock);
        assertThat(item.getSaleStart()).isEqualTo(saleStart);
        assertThat(item.getSaleEnd()).isEqualTo(saleEnd);
        assertThat(item.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("재고 차감 테스트")
    void decreaseStock() {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();

        // when
        item.decreaseStock(5);

        // then
        assertThat(item.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생 테스트")
    void decreaseStockWithInsufficientStock() {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(5)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();

        // when & then
        assertThatThrownBy(() -> item.decreaseStock(10))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("판매 중인 상품 확인 테스트")
    void isOnSale() {
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

        // when
        boolean isOnSale = item.isOnSale();

        // then
        assertThat(isOnSale).isTrue();
    }

    @Test
    @DisplayName("판매 종료된 상품 확인 테스트")
    void isNotOnSale() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now.minusDays(2))
            .saleEnd(now.minusDays(1))
            .build();

        // when
        boolean isOnSale = item.isOnSale();

        // then
        assertThat(isOnSale).isFalse();
    }

    @Test
    @DisplayName("판매 시작 전 상품 확인 테스트")
    void isNotOnSaleBeforeStart() {
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now.plusMinutes(1))
            .saleEnd(now.plusDays(1))
            .build();

        boolean isOnSale = item.isOnSale();

        assertThat(isOnSale).isFalse();
    }

    @Test
    @DisplayName("판매 시작 시각과 같은 경우 True 테스트")
    void isOnSaleAtStart() {
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now)
            .saleEnd(now.plusDays(1))
            .build();

        boolean isOnSale = item.isOnSale();

        assertThat(isOnSale).isTrue();
    }

    @Test
    @DisplayName("판매 종료 시각과 같은 경우 False 테스트")
    void isNotOnSaleAtEnd() {
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now.minusDays(1))
            .saleEnd(now)
            .build();

        boolean isOnSale = item.isOnSale();

        assertThat(isOnSale).isFalse();
    }

    @Test
    @DisplayName("판매 시작 시각과 종료 시각이 같은 경우 False 테스트")
    void isNotOnSaleWhenStartEqualsEnd() {
        LocalDateTime now = LocalDateTime.now();
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(10)
            .saleStart(now)
            .saleEnd(now)
            .build();

        boolean isOnSale = item.isOnSale();

        assertThat(isOnSale).isFalse();
    }
} 
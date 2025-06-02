package com.flash.item.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ItemExceptionTest {

    @Test
    @DisplayName("ItemNotFoundException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createItemNotFoundException() {
        // when
        ItemException.ItemNotFoundException exception = 
            new ItemException.ItemNotFoundException();

        // then
        assertThat(exception.getMessage()).isEqualTo("상품을 찾을 수 없습니다.");
        assertThat(exception.getCode()).isEqualTo("ITEM_NOT_FOUND");
    }

    @Test
    @DisplayName("ItemNotOnSaleException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createItemNotOnSaleException() {
        // when
        ItemException.ItemNotOnSaleException exception = 
            new ItemException.ItemNotOnSaleException();

        // then
        assertThat(exception.getMessage()).isEqualTo("판매 중이 아닌 상품입니다.");
        assertThat(exception.getCode()).isEqualTo("ITEM_NOT_ON_SALE");
    }

    @Test
    @DisplayName("OutOfStockException 생성 시 올바른 메시지와 코드가 설정되는지 확인")
    void createOutOfStockException() {
        // when
        ItemException.OutOfStockException exception = 
            new ItemException.OutOfStockException();

        // then
        assertThat(exception.getMessage()).isEqualTo("상품 재고가 부족합니다.");
        assertThat(exception.getCode()).isEqualTo("OUT_OF_STOCK");
    }
} 
package com.flash.item.exception;

import com.flash.common.exception.BaseException;

public class ItemException {
    public static class ItemNotFoundException extends BaseException {
        public ItemNotFoundException() {
            super("상품을 찾을 수 없습니다.", "ITEM_NOT_FOUND");
        }
    }
    
    public static class ItemNotOnSaleException extends BaseException {
        public ItemNotOnSaleException() {
            super("판매 중이 아닌 상품입니다.", "ITEM_NOT_ON_SALE");
        }
    }
    
    public static class OutOfStockException extends BaseException {
        public OutOfStockException() {
            super("상품 재고가 부족합니다.", "OUT_OF_STOCK");
        }
    }

    public ItemException() {
    }
} 
package com.flash.item.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

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

    @RepeatedTest(10)
    @DisplayName("동시성 이슈 테스트 - 재고가 음수가 되는 경우")
    void decreaseStock_concurrent_shouldCauseNegativeStock() throws InterruptedException {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();

        int threadCount = 200; // 200명이 동시에 구매
        int decreasePerThread = 1; // 각 스레드당 1개씩 구매
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // context switching 유도
                    Thread.sleep((long) (Math.random() * 10));
                    item.decreaseStockV2(decreasePerThread);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        int finalStock = item.getStock();
        System.out.println("최종 재고: " + finalStock);
        System.out.println("성공한 구매: " + successCount.get());
        System.out.println("실패한 구매: " + failCount.get());
        
        // 동시성 이슈가 해결되었다면 다음 조건들을 만족해야 함
        assertAll(
            () -> assertThat(finalStock).isEqualTo(0), // 재고가 정확히 0이어야 함
            () -> assertThat(successCount.get()).isEqualTo(100), // 성공한 구매가 초기 재고와 같아야 함
            () -> assertThat(finalStock + successCount.get()).isEqualTo(100), // 재고 + 성공한 구매가 초기 재고와 같아야 함
            () -> assertThat(finalStock).isGreaterThanOrEqualTo(0) // 재고가 음수가 아니어야 함
        );
    }

    @RepeatedTest(10)
    @DisplayName("동시성 테스트 - 100명이 100개 재고 구매")
    void decreaseStock_concurrent_shouldBeZero() throws InterruptedException {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();

        int threadCount = 100; // 100명이 동시에 구매
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 10));
                    item.decreaseStockV2(1);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        int finalStock = item.getStock();
        System.out.println("최종 재고: " + finalStock);
        
        // 동시성 이슈가 해결되었다면 재고가 정확히 0이어야 함
        assertThat(finalStock).isEqualTo(0);
    }
} 
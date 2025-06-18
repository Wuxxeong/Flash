package com.flash.item.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.flash.item.repository.ItemRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.OptimisticLockingFailureException;

@SpringBootTest
class ItemTest {

    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private PlatformTransactionManager transactionManager;

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

    @RepeatedTest(10)
    @DisplayName("동시성 테스트 - V3 버전 (Pessimistic Lock)")
    void decreaseStock_concurrent_V3() throws InterruptedException {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();
        itemRepository.save(item);

        int threadCount = 100; // 100명이 동시에 구매
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 10));
                    transactionTemplate.execute(status -> {
                        try {
                            Item foundItem = itemRepository.findByIdWithPessimisticLock(item.getId())
                                .orElseThrow(() -> new RuntimeException("Item not found"));
                            foundItem.decreaseStockV3(1);
                            itemRepository.save(foundItem);
                            successCount.incrementAndGet();
                            return null;
                        } catch (Exception e) {
                            status.setRollbackOnly();
                            throw e;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Item finalItem = itemRepository.findById(item.getId())
            .orElseThrow(() -> new RuntimeException("Item not found"));
        int finalStock = finalItem.getStock();
        
        System.out.println("최종 재고: " + finalStock);
        System.out.println("성공한 구매: " + successCount.get());
        System.out.println("실패한 구매: " + failCount.get());
        
        assertAll(
            () -> assertThat(finalStock).isEqualTo(0),
            () -> assertThat(successCount.get()).isEqualTo(100),
            () -> assertThat(finalStock + successCount.get()).isEqualTo(100),
            () -> assertThat(finalStock).isGreaterThanOrEqualTo(0)
        );
    }

    @Test
    @DisplayName("Optimistic Lock 기본 동작 테스트")
    void optimisticLockTest() {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();
        item = itemRepository.save(item);
        Long itemId = item.getId();

        // when
        Item firstItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        Item secondItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));

        firstItem.decreaseStockV4(1);
        itemRepository.save(firstItem);

        // then
        assertThatThrownBy(() -> {
            secondItem.decreaseStockV4(1);
            itemRepository.save(secondItem);
        }).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Optimistic Lock 버전 증가 테스트")
    void optimisticLockVersionTest() {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();
        item = itemRepository.save(item);
        Long itemId = item.getId();

        // when
        Item foundItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        Long initialVersion = foundItem.getVersion();

        foundItem.decreaseStockV4(1);
        itemRepository.save(foundItem);

        // then
        Item updatedItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        assertThat(updatedItem.getVersion()).isGreaterThan(initialVersion);
    }

    @RepeatedTest(10)
    @DisplayName("Optimistic Lock 동시성 테스트")
    /*
    Optimistic Lock의 문제점 : Version 필드를 사용하여 동시성 문제를 해결하지만,
    재시도 로직을 계속 수행하게 될 경우 수행시간에서 성능이 좋지 않다.
     */
    void optimisticLockConcurrentTest() throws InterruptedException {
        // given
        Item item = Item.builder()
            .name("테스트 상품")
            .description("테스트 설명")
            .price(10000)
            .stock(100)
            .saleStart(LocalDateTime.now())
            .saleEnd(LocalDateTime.now().plusDays(7))
            .build();
        item = itemRepository.save(item);
        Long itemId = item.getId();

        int threadCount = 100; // 100명이 동시에 구매
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 10)); // context switching 유도
                    boolean retry = true;
                    int retryCount = 0;
                    
                    while (retry && retryCount < 5) { // 최대 5번까지 재시도
                        try {
                            transactionTemplate.execute(status -> {
                                Item foundItem = itemRepository.findById(itemId)
                                    .orElseThrow(() -> new RuntimeException("Item not found"));
                                
                                if (foundItem.getStock() <= 0) {
                                    failCount.incrementAndGet();
                                    return null;
                                }
                                
                                foundItem.decreaseStockV4(1);
                                itemRepository.save(foundItem);
                                successCount.incrementAndGet();
                                return null;
                            });
                            retry = false; // 성공하면 재시도 중단
                        } catch (OptimisticLockingFailureException e) {
                            retryCount++;
                            if (retryCount >= 3) {
                                failCount.incrementAndGet();
                                System.out.println("최대 재시도 횟수 초과: " + e.getMessage());
                            }
                            Thread.sleep(100); // 충돌 시 잠시 대기
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Item finalItem = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        int finalStock = finalItem.getStock();
        
        System.out.println("최종 재고: " + finalStock);
        System.out.println("성공한 구매: " + successCount.get());
        System.out.println("실패한 구매: " + failCount.get());
        
        assertAll(
            () -> assertThat(finalStock).isEqualTo(0), // 재고가 정확히 0이어야 함
            () -> assertThat(successCount.get()).isEqualTo(100), // 성공한 구매가 초기 재고와 같아야 함
            () -> assertThat(finalStock + successCount.get()).isEqualTo(100), // 재고 + 성공한 구매가 초기 재고와 같아야 함
            () -> assertThat(finalStock).isGreaterThanOrEqualTo(0) // 재고가 음수가 아니어야 함
        );
    }
} 
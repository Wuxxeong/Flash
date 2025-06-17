package com.flash.item.repository;

import com.flash.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
    @Query("SELECT i FROM Item i WHERE i.saleStart <= :now AND i.saleEnd >= :now")
    List<Item> findAllOnSale(@Param("now") LocalDateTime now);
    
    Optional<Item> findOnSaleById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Optional<Item> findByIdWithPessimisticLock(@Param("id") Long id);
} 
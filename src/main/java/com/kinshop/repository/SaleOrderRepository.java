package com.kinshop.repository;

import com.kinshop.model.SaleOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {

    List<SaleOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<SaleOrder> findWithItemsById(Long id);

    @Query("select coalesce(sum(o.totalAmount), 0) from SaleOrder o where o.createdAt between :from and :to and o.status <> com.kinshop.model.SaleOrderStatus.CANCELLED")
    BigDecimal totalSalesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select coalesce(sum(o.discountAmount + o.itemDiscountAmount), 0) from SaleOrder o where o.createdAt between :from and :to and o.status <> com.kinshop.model.SaleOrderStatus.CANCELLED")
    BigDecimal totalDiscountsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select coalesce(sum(o.discountAmount), 0) from SaleOrder o where o.createdAt between :from and :to and o.status <> com.kinshop.model.SaleOrderStatus.CANCELLED")
    BigDecimal totalOrderDiscountsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select count(o) from SaleOrder o where o.createdAt between :from and :to and o.status <> com.kinshop.model.SaleOrderStatus.CANCELLED")
    long countOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

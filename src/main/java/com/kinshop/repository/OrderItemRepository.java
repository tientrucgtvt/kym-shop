package com.kinshop.repository;

import com.kinshop.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select coalesce(sum(((i.unitPrice - i.importPrice) * (i.quantity - i.returnedQuantity)) - ((i.discountAmount / i.quantity) * (i.quantity - i.returnedQuantity))), 0) " +
            "from OrderItem i " +
            "where i.order.createdAt between :from and :to " +
            "and i.order.status <> com.kinshop.model.SaleOrderStatus.CANCELLED")
    BigDecimal grossProfitBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

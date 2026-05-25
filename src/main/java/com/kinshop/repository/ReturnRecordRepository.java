package com.kinshop.repository;

import com.kinshop.model.ReturnRecord;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {

    @EntityGraph(attributePaths = {"order", "items", "items.product"})
    List<ReturnRecord> findAllByOrderByCreatedAtDesc();

    @Query("select coalesce(sum(r.refundAmount), 0) from ReturnRecord r where r.createdAt between :from and :to")
    BigDecimal totalRefundsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

package com.kinshop.service;

import java.math.BigDecimal;

public class ReportSummary {

    private final long orderCount;
    private final BigDecimal grossSales;
    private final BigDecimal discounts;
    private final BigDecimal refunds;
    private final BigDecimal netSettlement;
    private final BigDecimal grossProfit;

    public ReportSummary(
            long orderCount,
            BigDecimal grossSales,
            BigDecimal discounts,
            BigDecimal refunds,
            BigDecimal netSettlement,
            BigDecimal grossProfit
    ) {
        this.orderCount = orderCount;
        this.grossSales = grossSales;
        this.discounts = discounts;
        this.refunds = refunds;
        this.netSettlement = netSettlement;
        this.grossProfit = grossProfit;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getGrossSales() {
        return grossSales;
    }

    public BigDecimal getDiscounts() {
        return discounts;
    }

    public BigDecimal getRefunds() {
        return refunds;
    }

    public BigDecimal getNetSettlement() {
        return netSettlement;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }
}

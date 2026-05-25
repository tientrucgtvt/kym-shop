package com.kinshop.web;

import com.kinshop.repository.ProductRepository;
import com.kinshop.repository.ReturnRecordRepository;
import com.kinshop.repository.SaleOrderRepository;
import com.kinshop.repository.OrderItemRepository;
import com.kinshop.security.PermissionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ReturnRecordRepository returnRecordRepository;
    private final OrderItemRepository orderItemRepository;
    private final PermissionService permissionService;

    public DashboardController(
            ProductRepository productRepository,
            SaleOrderRepository saleOrderRepository,
            ReturnRecordRepository returnRecordRepository,
            OrderItemRepository orderItemRepository,
            PermissionService permissionService
    ) {
        this.productRepository = productRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.orderItemRepository = orderItemRepository;
        this.permissionService = permissionService;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        permissionService.require("DASHBOARD", "VIEW");
        var lowStockProducts = productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() <= product.getMinStockLevel())
                .collect(Collectors.toList());
        List<String> saleLabels = new ArrayList<>();
        List<BigDecimal> saleTotals = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate end = to == null ? today : to;
        LocalDate start = from == null ? end.minusDays(6) : from;
        if (start.isAfter(end)) {
            LocalDate originalStart = start;
            start = end;
            end = originalStart;
        }
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            saleLabels.add(day.format(DateTimeFormatter.ofPattern("MMM dd")));
            saleTotals.add(saleOrderRepository.totalSalesBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay().minusNanos(1)));
        }
        var startDateTime = start.atStartOfDay();
        var endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal totalSaleAmount = saleOrderRepository.totalSalesBetween(startDateTime, endDateTime);
        BigDecimal totalRefundAmount = returnRecordRepository.totalRefundsBetween(startDateTime, endDateTime);
        BigDecimal totalDiscountAmount = saleOrderRepository.totalDiscountsBetween(startDateTime, endDateTime);
        BigDecimal revenueAmount = totalSaleAmount.subtract(totalRefundAmount);
        BigDecimal profitAmount = orderItemRepository.grossProfitBetween(startDateTime, endDateTime)
                .subtract(saleOrderRepository.totalOrderDiscountsBetween(startDateTime, endDateTime));

        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("orderCount", saleOrderRepository.count());
        model.addAttribute("dashboardFrom", start);
        model.addAttribute("dashboardTo", end);
        model.addAttribute("totalSaleAmount", totalSaleAmount);
        model.addAttribute("totalRefundAmount", totalRefundAmount);
        model.addAttribute("totalDiscountAmount", totalDiscountAmount);
        model.addAttribute("revenueAmount", revenueAmount);
        model.addAttribute("profitAmount", profitAmount);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("saleChartLabels", saleLabels);
        model.addAttribute("saleChartValues", saleTotals);
        model.addAttribute("lowStockLabels", lowStockProducts.stream().map(product -> product.getName()).collect(Collectors.toList()));
        model.addAttribute("lowStockValues", lowStockProducts.stream().map(product -> product.getStockQuantity()).collect(Collectors.toList()));
        model.addAttribute("lowStockMinimums", lowStockProducts.stream().map(product -> product.getMinStockLevel()).collect(Collectors.toList()));
        return "index";
    }
}

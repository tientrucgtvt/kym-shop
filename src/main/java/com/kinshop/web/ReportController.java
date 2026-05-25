package com.kinshop.web;

import com.kinshop.security.PermissionService;
import com.kinshop.service.SaleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final SaleService saleService;
    private final PermissionService permissionService;

    public ReportController(SaleService saleService, PermissionService permissionService) {
        this.saleService = saleService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        permissionService.require("REPORT", "VIEW");
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.withDayOfMonth(1) : from;
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("summary", saleService.report(start, end));
        return "reports/index";
    }
}

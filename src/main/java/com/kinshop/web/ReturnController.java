package com.kinshop.web;

import com.kinshop.security.PermissionService;
import com.kinshop.service.SaleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/returns")
public class ReturnController {

    private final SaleService saleService;
    private final PermissionService permissionService;

    public ReturnController(SaleService saleService, PermissionService permissionService) {
        this.saleService = saleService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("RETURN", "VIEW");
        model.addAttribute("returns", saleService.findReturns());
        return "returns/list";
    }
}

package com.kinshop.web;

import com.kinshop.repository.StockMovementRepository;
import com.kinshop.security.PermissionService;
import com.kinshop.service.ProductService;
import com.kinshop.web.form.StockAdjustmentForm;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
public class StockController {

    private final ProductService productService;
    private final StockMovementRepository stockMovementRepository;
    private final PermissionService permissionService;

    public StockController(ProductService productService, StockMovementRepository stockMovementRepository, PermissionService permissionService) {
        this.productService = productService;
        this.stockMovementRepository = stockMovementRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String index(Model model) {
        permissionService.require("STOCK", "VIEW");
        model.addAttribute("form", new StockAdjustmentForm());
        model.addAttribute("products", productService.findActive());
        model.addAttribute("movements", stockMovementRepository.findAllByOrderByCreatedAtDesc());
        return "stock/index";
    }

    @PostMapping("/import")
    public String importStock(@Valid @ModelAttribute("form") StockAdjustmentForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        permissionService.require("STOCK", "STOCK");
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid product and quantity.");
            return "redirect:/stock";
        }
        try {
            productService.importStock(form.getProductId(), form.getQuantity(), form.getUnitCost(), form.getNote());
            redirectAttributes.addFlashAttribute("success", "Stock imported.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/stock";
    }

    @PostMapping("/export")
    public String exportStock(@Valid @ModelAttribute("form") StockAdjustmentForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        permissionService.require("STOCK", "STOCK");
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a valid product and quantity.");
            return "redirect:/stock";
        }
        try {
            productService.exportStock(form.getProductId(), form.getQuantity(), form.getNote());
            redirectAttributes.addFlashAttribute("success", "Stock exported.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/stock";
    }
}

package com.kinshop.web;

import com.kinshop.model.Product;
import com.kinshop.repository.BrandRepository;
import com.kinshop.repository.CategoryRepository;
import com.kinshop.security.PermissionService;
import com.kinshop.service.ProductService;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final PermissionService permissionService;

    public ProductController(
            ProductService productService,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            PermissionService permissionService
    ) {
        this.productService = productService;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("PRODUCT", "VIEW");
        model.addAttribute("products", productService.findAll());
        return "products/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        permissionService.require("PRODUCT", "CREATE");
        model.addAttribute("product", new Product());
        addProductOptions(model);
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        permissionService.require("PRODUCT", "UPDATE");
        model.addAttribute("product", productService.get(id));
        addProductOptions(model);
        return "products/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Product product, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        permissionService.require("PRODUCT", product.getId() == null ? "CREATE" : "UPDATE");
        if (bindingResult.hasErrors()) {
            addProductOptions(model);
            return "products/form";
        }
        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Product saved.");
        return "redirect:/products";
    }

    private void addProductOptions(Model model) {
        model.addAttribute("brands", brandRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
    }
}

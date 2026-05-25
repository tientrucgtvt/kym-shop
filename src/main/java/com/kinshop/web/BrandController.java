package com.kinshop.web;

import com.kinshop.model.Brand;
import com.kinshop.repository.BrandRepository;
import com.kinshop.security.PermissionService;
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
@RequestMapping("/brands")
public class BrandController {

    private final BrandRepository brandRepository;
    private final PermissionService permissionService;

    public BrandController(BrandRepository brandRepository, PermissionService permissionService) {
        this.brandRepository = brandRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("BRAND", "VIEW");
        model.addAttribute("brands", brandRepository.findAllByOrderByNameAsc());
        return "brands/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        permissionService.require("BRAND", "CREATE");
        model.addAttribute("brand", new Brand());
        return "brands/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        permissionService.require("BRAND", "UPDATE");
        model.addAttribute("brand", brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + id)));
        return "brands/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Brand brand, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        permissionService.require("BRAND", brand.getId() == null ? "CREATE" : "UPDATE");
        if (bindingResult.hasErrors()) {
            return "brands/form";
        }
        brandRepository.save(brand);
        redirectAttributes.addFlashAttribute("success", "Brand saved.");
        return "redirect:/brands";
    }
}

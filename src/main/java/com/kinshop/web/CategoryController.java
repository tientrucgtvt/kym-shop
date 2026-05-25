package com.kinshop.web;

import com.kinshop.model.Category;
import com.kinshop.repository.CategoryRepository;
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
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final PermissionService permissionService;

    public CategoryController(CategoryRepository categoryRepository, PermissionService permissionService) {
        this.categoryRepository = categoryRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("CATEGORY", "VIEW");
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        return "categories/list";
    }

    @GetMapping("/new")
    public String create(Model model) {
        permissionService.require("CATEGORY", "CREATE");
        model.addAttribute("category", new Category());
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        permissionService.require("CATEGORY", "UPDATE");
        model.addAttribute("category", categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)));
        return "categories/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Category category, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        permissionService.require("CATEGORY", category.getId() == null ? "CREATE" : "UPDATE");
        if (bindingResult.hasErrors()) {
            return "categories/form";
        }
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "Category saved.");
        return "redirect:/categories";
    }
}

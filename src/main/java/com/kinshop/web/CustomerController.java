package com.kinshop.web;

import com.kinshop.model.Customer;
import com.kinshop.repository.CustomerRepository;
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
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final PermissionService permissionService;

    public CustomerController(CustomerRepository customerRepository, PermissionService permissionService) {
        this.customerRepository = customerRepository;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("CUSTOMER", "VIEW");
        model.addAttribute("customers", customerRepository.findAllByOrderByNameAsc());
        return "customers/list";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        permissionService.require("CUSTOMER", "UPDATE");
        model.addAttribute("customer", customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id)));
        return "customers/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Customer customer, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        permissionService.require("CUSTOMER", "UPDATE");
        if (bindingResult.hasErrors()) {
            return "customers/form";
        }
        customerRepository.save(customer);
        redirectAttributes.addFlashAttribute("success", "Customer saved.");
        return "redirect:/customers";
    }
}

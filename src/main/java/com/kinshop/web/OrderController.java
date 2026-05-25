package com.kinshop.web;

import com.kinshop.model.Product;
import com.kinshop.repository.BrandRepository;
import com.kinshop.repository.CategoryRepository;
import com.kinshop.repository.CustomerRepository;
import com.kinshop.security.PermissionService;
import com.kinshop.service.ProductService;
import com.kinshop.service.SaleService;
import com.kinshop.web.form.OrderForm;
import com.kinshop.web.form.ReturnForm;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final String PENDING_ORDER_FORM = "pendingOrderForm";

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerRepository customerRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final FormatUtils formatUtils;
    private final PermissionService permissionService;

    public OrderController(
            SaleService saleService,
            ProductService productService,
            CustomerRepository customerRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            FormatUtils formatUtils,
            PermissionService permissionService
    ) {
        this.saleService = saleService;
        this.productService = productService;
        this.customerRepository = customerRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.formatUtils = formatUtils;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String list(Model model) {
        permissionService.require("SALE_ORDER", "VIEW");
        model.addAttribute("orders", saleService.findOrders());
        return "orders/list";
    }

    @GetMapping("/new")
    public String create(Model model, HttpSession session) {
        permissionService.require("SALE_ORDER", "CREATE");
        if (!model.containsAttribute("orderForm")) {
            OrderForm pendingOrderForm = (OrderForm) session.getAttribute(PENDING_ORDER_FORM);
            model.addAttribute("orderForm", pendingOrderForm == null ? new OrderForm() : pendingOrderForm);
        }
        addOrderFormOptions(model);
        return "orders/form";
    }

    @PostMapping
    public String save(@ModelAttribute OrderForm orderForm, RedirectAttributes redirectAttributes, HttpSession session) {
        permissionService.require("SALE_ORDER", "CREATE");
        try {
            var order = saleService.createOrder(orderForm);
            session.removeAttribute(PENDING_ORDER_FORM);
            redirectAttributes.addFlashAttribute("success", "Invoice created.");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalArgumentException ex) {
            session.setAttribute(PENDING_ORDER_FORM, orderForm);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("orderForm", orderForm);
            findInsufficientStockProduct(ex.getMessage())
                    .ifPresent(product -> {
                        redirectAttributes.addFlashAttribute("stockProductId", product.getId());
                        redirectAttributes.addFlashAttribute("stockProductName", product.getName());
                        redirectAttributes.addFlashAttribute("stockImportPrice", product.getImportPrice());
                    });
            return "redirect:/orders/new";
        } catch (DataAccessException ex) {
            session.setAttribute(PENDING_ORDER_FORM, orderForm);
            redirectAttributes.addFlashAttribute("error", "Could not save the order. Please check that amounts are valid and try again.");
            return "redirect:/orders/new";
        }
    }

    @PostMapping("/stock/import")
    public String importStockFromOrder(
            Long productId,
            int quantity,
            BigDecimal unitCost,
            RedirectAttributes redirectAttributes
    ) {
        permissionService.require("STOCK", "STOCK");
        try {
            productService.importStock(productId, quantity, unitCost, "Imported from order stock warning");
            redirectAttributes.addFlashAttribute("success", "Stock added. You can create the invoice now.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/new";
    }

    @PostMapping("/products")
    @ResponseBody
    public Map<String, Object> createProductFromOrder(
            @RequestParam String sku,
            @RequestParam String name,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal importPrice,
            @RequestParam BigDecimal salePrice,
            @RequestParam(defaultValue = "0") int minStockLevel,
            @RequestParam(defaultValue = "0") int initialStock
    ) {
        permissionService.require("PRODUCT", "CREATE");
        if (sku == null || sku.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("SKU and name are required.");
        }

        Product product = new Product();
        product.setSku(sku.trim());
        product.setName(name.trim());
        product.setDescription(description);
        product.setImportPrice(importPrice);
        product.setSalePrice(salePrice);
        product.setMinStockLevel(minStockLevel);
        product.setActive(true);
        if (brandId != null) {
            product.setBrand(brandRepository.findById(brandId)
                    .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + brandId)));
        }
        if (categoryId != null) {
            product.setCategory(categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId)));
        }

        Product saved = productService.save(product);
        if (initialStock > 0) {
            productService.importStock(saved.getId(), initialStock, importPrice, "Initial stock from order product creation");
            saved = productService.get(saved.getId());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("label", productLabel(saved));
        return response;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        permissionService.require("SALE_ORDER", "VIEW");
        model.addAttribute("order", saleService.getOrder(id));
        return "orders/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        permissionService.require("SALE_ORDER", "CANCEL");
        try {
            saleService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Order cancelled and stock restored.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @GetMapping("/{id}/return")
    public String returnForm(@PathVariable Long id, Model model) {
        permissionService.require("RETURN", "CREATE");
        model.addAttribute("order", saleService.getOrder(id));
        model.addAttribute("returnForm", new ReturnForm());
        return "orders/return-form";
    }

    @PostMapping("/{id}/return")
    public String returnItems(@PathVariable Long id, @ModelAttribute ReturnForm returnForm, RedirectAttributes redirectAttributes) {
        permissionService.require("RETURN", "CREATE");
        try {
            saleService.createReturn(id, returnForm);
            redirectAttributes.addFlashAttribute("success", "Return recorded and stock restored.");
            return "redirect:/orders/" + id;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders/" + id + "/return";
        }
    }

    private void addOrderFormOptions(Model model) {
        model.addAttribute("products", productService.findActive());
        model.addAttribute("customers", customerRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("brands", brandRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
    }

    private Optional<Product> findInsufficientStockProduct(String message) {
        String prefix = "Not enough stock for ";
        if (message == null || !message.startsWith(prefix)) {
            return Optional.empty();
        }
        String productName = message.substring(prefix.length());
        return productService.findActive().stream()
                .filter(product -> product.getName().equals(productName))
                .findFirst();
    }

    private String productLabel(Product product) {
        return product.getSku() + " - " + product.getName()
                + " | price: " + formatUtils.vnd(product.getSalePrice())
                + " | stock: " + formatUtils.qty(product.getStockQuantity());
    }
}

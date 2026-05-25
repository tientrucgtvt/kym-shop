package com.kinshop.service;

import com.kinshop.model.Product;
import com.kinshop.model.StockMovement;
import com.kinshop.model.StockMovementType;
import com.kinshop.repository.ProductRepository;
import com.kinshop.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(ProductRepository productRepository, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findActive() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional
    public Product save(Product product) {
        if (product.getId() == null) {
            return productRepository.save(product);
        }
        Product existing = get(product.getId());
        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setBrand(product.getBrand());
        existing.setCategory(product.getCategory());
        existing.setImportPrice(product.getImportPrice());
        existing.setSalePrice(product.getSalePrice());
        existing.setMinStockLevel(product.getMinStockLevel());
        existing.setActive(product.isActive());
        return productRepository.save(existing);
    }

    @Transactional
    public void importStock(Long productId, int quantity, BigDecimal unitCost, String note) {
        Product product = get(productId);
        product.setStockQuantity(product.getStockQuantity() + quantity);
        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) >= 0) {
            product.setImportPrice(unitCost);
        }
        productRepository.save(product);
        recordMovement(product, StockMovementType.IMPORT, quantity, product.getImportPrice(), "MANUAL_IMPORT", note);
    }

    @Transactional
    public void exportStock(Long productId, int quantity, String note) {
        Product product = get(productId);
        ensureStock(product, quantity);
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        recordMovement(product, StockMovementType.EXPORT, -quantity, product.getImportPrice(), "MANUAL_EXPORT", note);
    }

    @Transactional
    public void recordMovement(Product product, StockMovementType type, int quantity, BigDecimal unitCost, String reference, String note) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setStockAfter(product.getStockQuantity());
        movement.setUnitCost(unitCost == null ? BigDecimal.ZERO : unitCost);
        movement.setReference(reference);
        movement.setNote(note);
        stockMovementRepository.save(movement);
    }

    public void ensureStock(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for " + product.getName());
        }
    }
}

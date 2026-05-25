package com.kinshop.service;

import com.kinshop.model.OrderItem;
import com.kinshop.model.Product;
import com.kinshop.model.ReturnItem;
import com.kinshop.model.ReturnRecord;
import com.kinshop.model.SaleOrder;
import com.kinshop.model.SaleOrderStatus;
import com.kinshop.model.StockMovementType;
import com.kinshop.model.CustomerType;
import com.kinshop.model.Customer;
import com.kinshop.repository.CustomerRepository;
import com.kinshop.repository.OrderItemRepository;
import com.kinshop.repository.ProductRepository;
import com.kinshop.repository.ReturnRecordRepository;
import com.kinshop.repository.SaleOrderRepository;
import com.kinshop.web.form.OrderForm;
import com.kinshop.web.form.OrderLineForm;
import com.kinshop.web.form.ReturnForm;
import com.kinshop.web.form.ReturnLineForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class SaleService {

    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SaleOrderRepository saleOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ReturnRecordRepository returnRecordRepository;
    private final CustomerRepository customerRepository;
    private final ProductService productService;

    public SaleService(
            SaleOrderRepository saleOrderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ReturnRecordRepository returnRecordRepository,
            CustomerRepository customerRepository,
            ProductService productService
    ) {
        this.saleOrderRepository = saleOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.customerRepository = customerRepository;
        this.productService = productService;
    }

    public List<SaleOrder> findOrders() {
        return saleOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    public SaleOrder getOrder(Long id) {
        return saleOrderRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public List<ReturnRecord> findReturns() {
        return returnRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public SaleOrder createOrder(OrderForm form) {
        SaleOrder order = new SaleOrder();
        order.setInvoiceNumber("INV-" + LocalDateTime.now().format(NUMBER_DATE));
        Customer customer = resolveCustomer(form);
        applyCustomerSnapshot(order, customer, form);
        order.setDiscountAmount(nonNegative(form.getDiscountAmount()));

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal itemDiscountTotal = BigDecimal.ZERO;
        for (OrderLineForm line : form.getLines()) {
            if (line.getProductId() == null || line.getQuantity() <= 0) {
                continue;
            }

            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.getProductId()));
            productService.ensureStock(product, line.getQuantity());

            product.setStockQuantity(product.getStockQuantity() - line.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(line.getQuantity());
            item.setUnitPrice(product.getSalePrice());
            item.setImportPrice(product.getImportPrice());
            BigDecimal grossLineTotal = product.getSalePrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            BigDecimal itemDiscount = nonNegative(line.getDiscountAmount()).min(grossLineTotal);
            item.setDiscountAmount(itemDiscount);
            item.setLineTotal(grossLineTotal.subtract(itemDiscount));
            order.addItem(item);

            subtotal = subtotal.add(grossLineTotal);
            itemDiscountTotal = itemDiscountTotal.add(itemDiscount);
            productService.recordMovement(product, StockMovementType.SALE, -line.getQuantity(), product.getImportPrice(), order.getInvoiceNumber(), "Sale order");
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Add at least one order item");
        }

        order.setSubtotal(subtotal);
        order.setItemDiscountAmount(itemDiscountTotal);
        BigDecimal afterItemDiscount = subtotal.subtract(itemDiscountTotal);
        BigDecimal discount = order.getDiscountAmount().min(afterItemDiscount);
        order.setDiscountAmount(discount);
        order.setTotalAmount(afterItemDiscount.subtract(discount));
        return saleOrderRepository.save(order);
    }

    @Transactional
    public ReturnRecord createReturn(Long orderId, ReturnForm form) {
        SaleOrder order = getOrder(orderId);
        ReturnRecord returnRecord = new ReturnRecord();
        returnRecord.setReturnNumber("RET-" + LocalDateTime.now().format(NUMBER_DATE));
        returnRecord.setOrder(order);
        returnRecord.setNote(form.getNote());

        BigDecimal refundTotal = BigDecimal.ZERO;
        for (ReturnLineForm line : form.getLines()) {
            if (line.getOrderItemId() == null || line.getQuantity() <= 0) {
                continue;
            }
            OrderItem item = order.getItems().stream()
                    .filter(existing -> Objects.equals(existing.getId(), line.getOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Order item does not belong to this order"));
            if (line.getQuantity() > item.getReturnableQuantity()) {
                throw new IllegalArgumentException("Return quantity is greater than sold quantity for " + item.getProduct().getName());
            }

            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + line.getQuantity());
            productRepository.save(product);

            item.setReturnedQuantity(item.getReturnedQuantity() + line.getQuantity());
            orderItemRepository.save(item);

            BigDecimal netUnitPrice = item.getLineTotal()
                    .divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP);
            BigDecimal refundAmount = netUnitPrice.multiply(BigDecimal.valueOf(line.getQuantity()));
            ReturnItem returnItem = new ReturnItem();
            returnItem.setOrderItem(item);
            returnItem.setProduct(product);
            returnItem.setQuantity(line.getQuantity());
            returnItem.setRefundAmount(refundAmount);
            returnRecord.addItem(returnItem);

            refundTotal = refundTotal.add(refundAmount);
            productService.recordMovement(product, StockMovementType.RETURN, line.getQuantity(), item.getImportPrice(), returnRecord.getReturnNumber(), "Customer return");
        }

        if (returnRecord.getItems().isEmpty()) {
            throw new IllegalArgumentException("Add at least one return item");
        }

        returnRecord.setRefundAmount(refundTotal);
        order.setRefundedAmount(order.getRefundedAmount().add(refundTotal));
        boolean allReturned = order.getItems().stream().allMatch(item -> item.getReturnableQuantity() == 0);
        order.setStatus(allReturned ? SaleOrderStatus.REFUNDED : SaleOrderStatus.PARTIALLY_REFUNDED);
        saleOrderRepository.save(order);
        return returnRecordRepository.save(returnRecord);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        SaleOrder order = getOrder(orderId);
        if (order.getStatus() == SaleOrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order is already cancelled.");
        }
        if (order.getRefundedAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Cannot cancel an order that already has returns. Use refund/return instead.");
        }

        for (OrderItem item : order.getItems()) {
            int restoreQuantity = item.getReturnableQuantity();
            if (restoreQuantity <= 0) {
                continue;
            }
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + restoreQuantity);
            productRepository.save(product);
            productService.recordMovement(
                    product,
                    StockMovementType.RETURN,
                    restoreQuantity,
                    item.getImportPrice(),
                    order.getInvoiceNumber(),
                    "Order cancelled"
            );
        }

        order.setStatus(SaleOrderStatus.CANCELLED);
        saleOrderRepository.save(order);
    }

    public ReportSummary report(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal sales = saleOrderRepository.totalSalesBetween(fromDateTime, toDateTime);
        BigDecimal discounts = saleOrderRepository.totalDiscountsBetween(fromDateTime, toDateTime);
        BigDecimal refunds = returnRecordRepository.totalRefundsBetween(fromDateTime, toDateTime);
        BigDecimal profit = orderItemRepository.grossProfitBetween(fromDateTime, toDateTime)
                .subtract(saleOrderRepository.totalOrderDiscountsBetween(fromDateTime, toDateTime));
        return new ReportSummary(
                saleOrderRepository.countOrdersBetween(fromDateTime, toDateTime),
                sales,
                discounts,
                refunds,
                sales.subtract(refunds),
                profit
        );
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private CustomerType parseCustomerType(String customerType) {
        if (customerType == null || customerType.isBlank()) {
            return CustomerType.INDIVIDUAL;
        }
        return CustomerType.valueOf(customerType);
    }

    private Customer resolveCustomer(OrderForm form) {
        if (form.getCustomerId() != null) {
            return customerRepository.findById(form.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + form.getCustomerId()));
        }

        CustomerType type = parseCustomerType(form.getCustomerType());
        String name = clean(form.getCustomerName());
        String taxNumber = clean(form.getCustomerTaxNumber());
        String address = clean(form.getCustomerAddress());
        String phone = clean(form.getCustomerPhone());
        if (isBlank(name) && isBlank(taxNumber) && isBlank(address) && isBlank(phone)) {
            return null;
        }

        Customer customer = null;
        if (!isBlank(taxNumber)) {
            customer = customerRepository.findFirstByTaxNumberIgnoreCase(taxNumber).orElse(null);
        }
        if (customer == null && !isBlank(phone)) {
            customer = customerRepository.findFirstByPhoneNumber(phone).orElse(null);
        }
        if (customer == null && !isBlank(name)) {
            customer = customerRepository.findFirstByTypeAndNameIgnoreCase(type, name).orElse(null);
        }
        if (customer == null) {
            customer = new Customer();
        }

        customer.setType(type);
        customer.setName(isBlank(name) ? "Walk-in customer" : name);
        customer.setTaxNumber(taxNumber);
        customer.setAddress(address);
        customer.setPhoneNumber(phone);
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    private void applyCustomerSnapshot(SaleOrder order, Customer customer, OrderForm form) {
        if (customer == null) {
            order.setCustomerType(parseCustomerType(form.getCustomerType()));
            order.setCustomerName(form.getCustomerName());
            order.setCustomerTaxNumber(form.getCustomerTaxNumber());
            order.setCustomerAddress(form.getCustomerAddress());
            order.setCustomerPhone(form.getCustomerPhone());
            return;
        }
        order.setCustomer(customer);
        order.setCustomerType(customer.getType());
        order.setCustomerName(customer.getName());
        order.setCustomerTaxNumber(customer.getTaxNumber());
        order.setCustomerAddress(customer.getAddress());
        order.setCustomerPhone(customer.getPhoneNumber());
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

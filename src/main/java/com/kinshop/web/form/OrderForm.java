package com.kinshop.web.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderForm {

    private Long customerId;

    private String customerType = "INDIVIDUAL";

    private String customerName;

    private String customerTaxNumber;

    private String customerAddress;

    private String customerPhone;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private List<OrderLineForm> lines = new ArrayList<>();

    public OrderForm() {
        for (int i = 0; i < 5; i++) {
            lines.add(new OrderLineForm());
        }
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerTaxNumber() {
        return customerTaxNumber;
    }

    public void setCustomerTaxNumber(String customerTaxNumber) {
        this.customerTaxNumber = customerTaxNumber;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<OrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineForm> lines) {
        this.lines = lines;
    }
}

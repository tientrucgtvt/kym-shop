package com.kinshop.security;

import java.util.Arrays;
import java.util.List;

public enum AppPage {
    DASHBOARD("Dashboard"),
    PRODUCT("Products"),
    BRAND("Brands"),
    CATEGORY("Categories"),
    STOCK("Stock"),
    SALE_ORDER("Orders"),
    CUSTOMER("Customers"),
    RETURN("Returns"),
    REPORT("Reports"),
    ACCESS_RIGHT("Access Rights");

    private final String label;

    AppPage(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public static List<AppPage> all() {
        return Arrays.asList(values());
    }
}

package com.kinshop.web;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Component("formatUtils")
public class FormatUtils {

    private static final Locale VIETNAM = new Locale("vi", "VN");

    public String vnd(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(VIETNAM);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(amount == null ? BigDecimal.ZERO : amount);
    }

    public String qty(Number quantity) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(VIETNAM);
        return formatter.format(quantity == null ? 0 : quantity);
    }
}

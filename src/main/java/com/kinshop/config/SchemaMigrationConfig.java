package com.kinshop.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SchemaMigrationConfig {

    @Bean
    ApplicationRunner widenMoneyColumns(JdbcTemplate jdbcTemplate) {
        return args -> {
            alterDecimal(jdbcTemplate, "product", "import_price");
            alterDecimal(jdbcTemplate, "product", "sale_price");
            alterDecimal(jdbcTemplate, "stock_movement", "unit_cost");

            alterDecimal(jdbcTemplate, "sale_order", "subtotal");
            alterDecimal(jdbcTemplate, "sale_order", "item_discount_amount");
            alterDecimal(jdbcTemplate, "sale_order", "discount_amount");
            alterDecimal(jdbcTemplate, "sale_order", "total_amount");
            alterDecimal(jdbcTemplate, "sale_order", "refunded_amount");

            alterDecimal(jdbcTemplate, "order_item", "unit_price");
            alterDecimal(jdbcTemplate, "order_item", "import_price");
            alterDecimal(jdbcTemplate, "order_item", "discount_amount");
            alterDecimal(jdbcTemplate, "order_item", "line_total");

            alterDecimal(jdbcTemplate, "return_record", "refund_amount");
            alterDecimal(jdbcTemplate, "return_item", "refund_amount");
        };
    }

    private void alterDecimal(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        try {
            jdbcTemplate.execute("alter table " + tableName + " modify column " + columnName + " decimal(19,2) not null");
        } catch (RuntimeException ignored) {
            // Some fresh databases may not have every table/column before Hibernate finishes updating.
        }
    }
}

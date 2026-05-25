package com.kinshop.web;

import com.kinshop.model.Brand;
import com.kinshop.model.Category;
import com.kinshop.repository.BrandRepository;
import com.kinshop.repository.CategoryRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public WebConfig(BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToBrandConverter(brandRepository));
        registry.addConverter(new StringToCategoryConverter(categoryRepository));
    }

    private static class StringToBrandConverter implements Converter<String, Brand> {
        private final BrandRepository brandRepository;

        private StringToBrandConverter(BrandRepository brandRepository) {
            this.brandRepository = brandRepository;
        }

        @Override
        public Brand convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return brandRepository.findById(Long.valueOf(source))
                    .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + source));
        }
    }

    private static class StringToCategoryConverter implements Converter<String, Category> {
        private final CategoryRepository categoryRepository;

        private StringToCategoryConverter(CategoryRepository categoryRepository) {
            this.categoryRepository = categoryRepository;
        }

        @Override
        public Category convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            return categoryRepository.findById(Long.valueOf(source))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + source));
        }
    }
}

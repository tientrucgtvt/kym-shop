package com.kinshop.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class CustomerTypeConverter implements AttributeConverter<CustomerType, String> {

    @Override
    public String convertToDatabaseColumn(CustomerType attribute) {
        return attribute == null ? CustomerType.INDIVIDUAL.name() : attribute.name();
    }

    @Override
    public CustomerType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return CustomerType.INDIVIDUAL;
        }
        return CustomerType.valueOf(dbData);
    }
}

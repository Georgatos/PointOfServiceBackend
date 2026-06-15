package dev.andreasgeorgatos.pointofservicebackend.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {

    @Override
    public String convertToDatabaseColumn(Category category) {
        return category != null ? category.getDatabaseCode() : null;
    }

    @Override
    public Category convertToEntityAttribute(String databaseCode) {
        return databaseCode != null ? Category.fromDatabaseCode(databaseCode) : null;
    }
}

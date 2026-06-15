package dev.andreasgeorgatos.pointofservicebackend.enums;

import lombok.Getter;

@Getter
public enum Category {
    FRUITS("FRUIT", "Fresh Fruits"),
    VEGETABLES("VEG", "Fresh Vegetables"),
    HERBS_AND_SPICES("HERB_SPICE", "Herbs & Spices"),
    MEAT("MEAT", "Meats"),
    POULTRY("POULTRY", "Poultry"),
    SEAFOOD("SEAFOOD", "Seafood"),
    DAIRY_AND_EGGS("DAIRY_EGG", "Dairy & Eggs"),
    GRAINS_AND_PASTA("GRAIN_PASTA", "Grains & Pasta"),
    BAKERY_AND_BREAD("BAKERY", "Bakery & Bread"),
    OILS_AND_CONDIMENTS("OIL_COND", "Oils & Condiments"),
    SAUCES_AND_DRESSINGS("SAUCE_DRESS", "Sauces & Dressings"),
    BEVERAGES("BEV", "Beverages"),
    SWEETENERS_AND_BAKING("BAKE_SUPPLY", "Sweeteners & Baking"),
    PACKAGED_GOODS("PACKAGED", "Packaged Goods");

    private final String databaseCode;
    private final String displayName;


    public static Category fromDatabaseCode(String databaseCode) {
        for (Category category : Category.values()) {
            if (category.getDatabaseCode().equals(databaseCode)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown database code: " + databaseCode);

    }

    Category(String dbCode, String displayName) {
        this.databaseCode = dbCode;
        this.displayName = displayName;
    }
}
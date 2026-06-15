package dev.andreasgeorgatos.pointofservicebackend.enums;

import lombok.Getter;

@Getter
public enum Allergen {
    MILK("MILK", "Dairy / Milk"),
    EGGS("EGG", "Eggs"),
    FISH("FISH", "Fish"),
    CRUSTACEAN_SHELLFISH("SHELLFISH", "Crustacean Shellfish"),
    TREE_NUTS("TREENUT", "Tree Nuts"),
    PEANUTS("PEANUT", "Peanuts"),
    WHEAT("WHEAT", "Wheat"),
    SOYBEANS("SOY", "Soybeans"),
    SESAME("SESAME", "Sesame"),
    SULFITES("SULFITE", "Sulfites"),
    MUSTARD("MUSTARD", "Mustard"),
    CELERY("CELERY", "Celery"),
    LUPIN("LUPIN", "Lupin"),
    MOLLUSCS("MOLLUSC", "Molluscs");

    private final String databaseCode;
    private final String displayName;

    Allergen(String dbCode, String displayName) {
        this.databaseCode = dbCode;
        this.displayName = displayName;
    }

    public static Allergen fromDatabaseCode(String databaseCode) {
        for (Allergen allergen : Allergen.values()) {
            if (allergen.getDatabaseCode().equals(databaseCode)) {
                return allergen;
            }
        }
        throw new IllegalArgumentException("Unknown database code: " + databaseCode);
    }
}
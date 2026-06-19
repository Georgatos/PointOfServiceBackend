package dev.andreasgeorgatos.pointofservicebackend.enums;

public enum PaymentStatus {
    PENDING("Pending", "Pending"),
    COMPLETED("Completed", "Completed"),
    FAILED("Failed", "Failed"),
    REFUNDED("Refunded", "Refunded");


    private final String databaseCode;
    private final String displayName;

    PaymentStatus(String databaseCode, String displayName) {
        this.databaseCode = databaseCode;
        this.displayName = displayName;
    }
}

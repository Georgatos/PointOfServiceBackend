package dev.andreasgeorgatos.pointofservicebackend.enums;

public enum OrderStatus {

    OPEN("Open", "Open"),
    IN_PROGRESS("In_progress", "In_progress"),
    READY("Ready", "Ready"),
    PAID("Paid", "Paid"),
    REFUNDED("Refunded", "Refunded"),
    VOIDED("Voided", "Voided");

    private final String databaseCode;
    private final String displayName;

    OrderStatus(String databaseCode, String displayName) {
        this.databaseCode = databaseCode;
        this.displayName = displayName;
    }
}

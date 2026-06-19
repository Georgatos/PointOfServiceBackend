package dev.andreasgeorgatos.pointofservicebackend.enums;

public enum PaymentMethod {
    CASH("Cash", "Cash"),
    CARD("Card", "Card"),
    MOBILE("Mobile", "Mobile"),
    POINTS("Points", "Points"),
    VOUCHER("Voucher", "Voucher");

    private final String databaseCode;
    private final String displayName;

    PaymentMethod(String databaseCode, String displayName) {
        this.databaseCode = databaseCode;
        this.displayName = displayName;
    }

}

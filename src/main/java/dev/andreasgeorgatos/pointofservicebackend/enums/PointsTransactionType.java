package dev.andreasgeorgatos.pointofservicebackend.enums;

import lombok.Getter;

@Getter
public enum PointsTransactionType {
    EARN("Earn", "Earn"),
    REDEEM("Redeem", "Redeem"),
    EXPIRE("Expire", "Expirie"),
    ADJUST("Adjust", "Adjust");

    private final String databaseCode;
    private final String displayName;

    PointsTransactionType(String dbCode, String displayName) {
        this.databaseCode = dbCode;
        this.displayName = displayName;
    }

}

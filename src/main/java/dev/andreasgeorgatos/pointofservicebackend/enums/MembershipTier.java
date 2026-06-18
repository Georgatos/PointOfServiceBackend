package dev.andreasgeorgatos.pointofservicebackend.enums;

import lombok.Getter;

@Getter
public enum MembershipTier {
    BRONZE("Bronze", "Bronze"),
    SILVER("Silver", "Silver"),
    GOLD("Gold", "Gold"),
    PLATINUM("Platinum", "Platinum");

    private String databaseCode;
    private String displayName;

    MembershipTier(String databaseCode, String displayName) {
        this.databaseCode = databaseCode;
        this.displayName = displayName;
    }
}

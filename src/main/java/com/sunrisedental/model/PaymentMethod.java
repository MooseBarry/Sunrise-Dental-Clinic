package com.sunrisedental.model;

public enum PaymentMethod {

    CASH("Cash"),
    CARD("Card"),
    BANK_TRANSFER("Bank transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
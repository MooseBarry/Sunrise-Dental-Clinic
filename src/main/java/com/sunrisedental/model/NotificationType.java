package com.sunrisedental.model;

public enum NotificationType {
    APPOINTMENT("Appointment"),
    BILLING("Billing"),
    PAYMENT("Payment"),
    SYSTEM("System");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

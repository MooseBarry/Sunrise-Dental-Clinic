package com.sunrisedental.model;

import java.util.EnumSet;
import java.util.Set;

public enum Role {

    ADMIN(EnumSet.allOf(Permission.class)),

    RECEPTIONIST(EnumSet.of(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_PATIENTS,
            Permission.MANAGE_PATIENTS,
            Permission.VIEW_APPOINTMENTS,
            Permission.MANAGE_APPOINTMENTS,
            Permission.UPDATE_APPOINTMENT_STATUS,
            Permission.VIEW_NOTIFICATIONS,
            Permission.VIEW_HELP
    )),

    DENTIST(EnumSet.of(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_APPOINTMENTS,
            Permission.UPDATE_APPOINTMENT_STATUS,
            Permission.VIEW_NOTIFICATIONS,
            Permission.VIEW_HELP
    )),

    CASHIER(EnumSet.of(
            Permission.VIEW_DASHBOARD,
            Permission.VIEW_BILLING,
            Permission.MANAGE_BILLING,
            Permission.VIEW_NOTIFICATIONS,
            Permission.VIEW_HELP
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public boolean allows(Permission permission) {
        return permission != null && permissions.contains(permission);
    }

    public String displayName() {
        return switch (this) {
            case ADMIN -> "Administrator";
            case RECEPTIONIST -> "Receptionist";
            case DENTIST -> "Dentist";
            case CASHIER -> "Cashier";
        };
    }
}

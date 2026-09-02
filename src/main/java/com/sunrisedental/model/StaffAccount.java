package com.sunrisedental.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record StaffAccount(
        long userId,
        String username,
        String fullName,
        String email,
        String contactNumber,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public String createdAtDisplay() {
        return createdAt == null ? "" : createdAt.format(FORMAT);
    }
}

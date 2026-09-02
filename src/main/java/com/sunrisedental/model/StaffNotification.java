package com.sunrisedental.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record StaffNotification(
        long notificationId,
        long recipientUserId,
        NotificationType notificationType,
        String title,
        String message,
        String referenceType,
        String referenceValue,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public boolean unread() {
        return readAt == null;
    }

    public String createdAtDisplay() {
        return createdAt == null ? "" : createdAt.format(FORMAT);
    }
}

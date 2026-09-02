package com.sunrisedental.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AuditEntry(
        long auditId,
        String actorName,
        String actionName,
        String entityType,
        String entityReference,
        String details,
        LocalDateTime createdAt
) {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public String createdAtDisplay() {
        return createdAt == null ? "" : createdAt.format(FORMAT);
    }
}

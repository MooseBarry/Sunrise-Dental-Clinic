package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record DailyRevenue(
        LocalDate date,
        BigDecimal amount
) {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd MMM");

    public String dateDisplay() {
        return date == null ? "" : date.format(FORMAT);
    }
}

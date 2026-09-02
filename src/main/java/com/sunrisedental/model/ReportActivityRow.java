package com.sunrisedental.model;

import java.math.BigDecimal;

public record ReportActivityRow(
        String label,
        long activityCount,
        BigDecimal amount
) {
}

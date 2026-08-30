package com.sunrisedental.model;

import java.math.BigDecimal;

public record Treatment(
        long treatmentId,
        String treatmentCode,
        String treatmentName,
        String description,
        BigDecimal standardFee
) {
}
package com.sunrisedental.model;

import java.math.BigDecimal;

public record Dentist(
        long dentistId,
        String fullName,
        String registrationNumber,
        String specialization,
        BigDecimal consultationFee
) {
}
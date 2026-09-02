package com.sunrisedental.model;

import java.math.BigDecimal;

public record BillingSource(
        long appointmentId,
        String appointmentNumber,
        AppointmentStatus appointmentStatus,
        BigDecimal consultationFee,
        BigDecimal treatmentTotal
) {
}
package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentDetails(
        long appointmentId,
        String appointmentNumber,
        String patientNumber,
        String patientName,
        String patientAddress,
        String patientContact,
        String dentistName,
        String dentistRegistrationNumber,
        String treatmentName,
        int treatmentQuantity,
        BigDecimal chargedFee,
        LocalDate appointmentDate,
        LocalTime startTime,
        int durationMinutes,
        String reason,
        AppointmentStatus status,
        String notes,
        String createdByName,
        LocalDateTime createdAt
) {
    public BigDecimal treatmentTotal() {
        return chargedFee.multiply(
                BigDecimal.valueOf(treatmentQuantity)
        );
    }
}
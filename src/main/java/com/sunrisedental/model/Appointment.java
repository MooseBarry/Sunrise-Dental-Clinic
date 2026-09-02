package com.sunrisedental.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Appointment(
        long appointmentId,
        String appointmentNumber,
        long patientId,
        long dentistId,
        long treatmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        int durationMinutes,
        String reason,
        AppointmentStatus status,
        String notes,
        long createdBy
) {
}
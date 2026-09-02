package com.sunrisedental.service;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRegistrationRequest(
        long patientId,
        long dentistId,
        long treatmentId,
        LocalDate appointmentDate,
        LocalTime startTime,
        int durationMinutes,
        String reason,
        String notes,
        long createdBy
) {
}
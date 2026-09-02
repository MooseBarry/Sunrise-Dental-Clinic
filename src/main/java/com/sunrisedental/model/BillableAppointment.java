package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record BillableAppointment(
        long appointmentId,
        String appointmentNumber,
        String patientNumber,
        String patientName,
        String dentistName,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String treatmentSummary,
        BigDecimal estimatedTotal
) {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    public String optionLabel() {
        return appointmentNumber
                + " | "
                + patientName
                + " | "
                + appointmentDate.format(DATE_FORMAT)
                + " "
                + appointmentTime.format(TIME_FORMAT)
                + " | LKR "
                + estimatedTotal.toPlainString();
    }
}
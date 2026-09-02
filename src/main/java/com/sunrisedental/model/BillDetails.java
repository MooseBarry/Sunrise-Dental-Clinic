package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record BillDetails(
        long billId,
        String billNumber,
        long appointmentId,
        String appointmentNumber,
        String patientNumber,
        String patientName,
        String patientContact,
        String patientEmail,
        String patientAddress,
        String dentistName,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String treatmentSummary,
        BigDecimal consultationFee,
        BigDecimal treatmentTotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        PaymentStatus paymentStatus,
        String issuedByName,
        LocalDateTime issuedAt
) {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );

    public BigDecimal outstandingAmount() {
        return totalAmount.subtract(amountPaid);
    }

    public String appointmentDateDisplay() {
        return appointmentDate == null
                ? ""
                : appointmentDate.format(DATE_FORMAT);
    }

    public String appointmentTimeDisplay() {
        return appointmentTime == null
                ? ""
                : appointmentTime.format(TIME_FORMAT);
    }

    public String issuedAtDisplay() {
        return issuedAt == null
                ? ""
                : issuedAt.format(DATE_TIME_FORMAT);
    }
}
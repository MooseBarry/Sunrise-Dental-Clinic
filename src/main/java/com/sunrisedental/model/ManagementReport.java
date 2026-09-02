package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ManagementReport(
        LocalDate fromDate,
        LocalDate toDate,
        long totalAppointments,
        long scheduledAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long noShowAppointments,
        long newPatients,
        BigDecimal invoicedAmount,
        BigDecimal receivedAmount,
        BigDecimal outstandingAmount,
        List<ReportActivityRow> treatmentActivity,
        List<ReportActivityRow> dentistActivity,
        List<DailyRevenue> dailyRevenue
) {
}

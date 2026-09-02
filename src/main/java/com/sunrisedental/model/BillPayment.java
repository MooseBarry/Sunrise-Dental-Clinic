package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record BillPayment(
        long paymentId,
        String receiptNumber,
        long billId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        long receivedBy,
        LocalDateTime paidAt
) {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
            );

    public String paidAtDisplay() {
        return paidAt == null
                ? ""
                : paidAt.format(DATE_TIME_FORMAT);
    }
}
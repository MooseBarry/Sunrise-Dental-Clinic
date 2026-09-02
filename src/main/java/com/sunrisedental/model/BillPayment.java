package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillPayment(
        long paymentId,
        String receiptNumber,
        long billId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        long receivedBy,
        LocalDateTime paidAt
) {
}
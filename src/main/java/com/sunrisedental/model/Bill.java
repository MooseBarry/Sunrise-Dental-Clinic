package com.sunrisedental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Bill(
        long billId,
        String billNumber,
        long appointmentId,
        BigDecimal consultationFee,
        BigDecimal treatmentTotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        PaymentStatus paymentStatus,
        long issuedBy,
        LocalDateTime issuedAt,
        LocalDateTime updatedAt
) {

    public BigDecimal outstandingAmount() {
        return totalAmount.subtract(amountPaid);
    }
}
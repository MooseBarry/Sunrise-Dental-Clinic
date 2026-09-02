package com.sunrisedental.service;

import com.sunrisedental.dao.BillDao;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.BillPayment;
import com.sunrisedental.model.BillingSource;
import com.sunrisedental.model.PaymentMethod;
import com.sunrisedental.model.PaymentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class BillingService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING =
            RoundingMode.HALF_UP;

    private final BillDao billDao;

    public BillingService(BillDao billDao) {
        if (billDao == null) {
            throw new IllegalArgumentException(
                    "Bill DAO is required."
            );
        }

        this.billDao = billDao;
    }

    public Bill generateBill(
            String appointmentNumber,
            BigDecimal discountAmount,
            long issuedBy
    ) {
        String normalizedAppointmentNumber =
                normalizeReference(
                        appointmentNumber,
                        "Appointment number"
                );

        validateStaffId(issuedBy);

        BillingSource source = billDao
                .findBillingSourceByAppointmentNumber(
                        normalizedAppointmentNumber
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Appointment was not found."
                        )
                );

        if (source.appointmentStatus()
                != AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Only completed appointments can be billed."
            );
        }

        if (billDao.existsByAppointmentId(
                source.appointmentId()
        )) {
            throw new IllegalArgumentException(
                    "A bill already exists for this appointment."
            );
        }

        BigDecimal consultationFee =
                normaliseDatabaseAmount(
                        source.consultationFee()
                );

        BigDecimal treatmentTotal =
                normaliseDatabaseAmount(
                        source.treatmentTotal()
                );

        BigDecimal subtotal =
                consultationFee.add(treatmentTotal);

        if (subtotal.signum() <= 0) {
            throw new IllegalArgumentException(
                    "The appointment does not contain " +
                            "any billable charges."
            );
        }

        BigDecimal discount =
                normaliseDiscount(discountAmount);

        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Discount cannot exceed the bill subtotal."
            );
        }

        BigDecimal totalAmount =
                subtotal.subtract(discount)
                        .setScale(
                                MONEY_SCALE,
                                MONEY_ROUNDING
                        );

        PaymentStatus initialStatus =
                totalAmount.signum() == 0
                        ? PaymentStatus.PAID
                        : PaymentStatus.UNPAID;

        String billNumber =
                generateReference("INV");

        Bill newBill = new Bill(
                0,
                billNumber,
                source.appointmentId(),
                consultationFee,
                treatmentTotal,
                discount,
                totalAmount,
                BigDecimal.ZERO.setScale(MONEY_SCALE),
                initialStatus,
                issuedBy,
                null,
                null
        );

        long billId = billDao.create(newBill);

        return billDao.findByBillNumber(billNumber)
                .orElse(
                        new Bill(
                                billId,
                                billNumber,
                                source.appointmentId(),
                                consultationFee,
                                treatmentTotal,
                                discount,
                                totalAmount,
                                BigDecimal.ZERO.setScale(
                                        MONEY_SCALE
                                ),
                                initialStatus,
                                issuedBy,
                                null,
                                null
                        )
                );
    }

    public Bill recordPayment(
            String billNumber,
            BigDecimal paymentAmount,
            PaymentMethod paymentMethod,
            long receivedBy
    ) {
        String normalizedBillNumber =
                normalizeReference(
                        billNumber,
                        "Bill number"
                );

        validateStaffId(receivedBy);

        if (paymentMethod == null) {
            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }

        BigDecimal amount =
                normalisePaymentAmount(paymentAmount);

        Bill bill = billDao
                .findByBillNumber(normalizedBillNumber)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Bill was not found."
                        )
                );

        if (bill.paymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "This bill has already been paid."
            );
        }

        if (amount.compareTo(
                bill.outstandingAmount()
        ) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds the outstanding balance."
            );
        }

        BillPayment payment = new BillPayment(
                0,
                generateReference("RCT"),
                bill.billId(),
                amount,
                paymentMethod,
                receivedBy,
                null
        );

        billDao.recordPayment(payment);

        return billDao
                .findByBillNumber(normalizedBillNumber)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Updated bill could not be loaded."
                        )
                );
    }

    public Optional<Bill> findByBillNumber(
            String billNumber
    ) {
        if (billNumber == null || billNumber.isBlank()) {
            return Optional.empty();
        }

        return billDao.findByBillNumber(
                billNumber.trim()
                        .toUpperCase(Locale.ROOT)
        );
    }

    public Optional<Bill> findByAppointmentNumber(
            String appointmentNumber
    ) {
        if (appointmentNumber == null
                || appointmentNumber.isBlank()) {
            return Optional.empty();
        }

        return billDao.findByAppointmentNumber(
                appointmentNumber.trim()
                        .toUpperCase(Locale.ROOT)
        );
    }

    public List<Bill> getAllBills() {
        return billDao.findAll();
    }

    public List<BillPayment> getPayments(long billId) {
        if (billId <= 0) {
            throw new IllegalArgumentException(
                    "A valid bill ID is required."
            );
        }

        return billDao.findPaymentsByBillId(billId);
    }

    private BigDecimal normaliseDatabaseAmount(
            BigDecimal amount
    ) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE);
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Billing charges cannot be negative."
            );
        }

        return amount.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING
        );
    }

    private BigDecimal normaliseDiscount(
            BigDecimal discount
    ) {
        if (discount == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE);
        }

        BigDecimal normalized = discount.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING
        );

        if (normalized.signum() < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be negative."
            );
        }

        return normalized;
    }

    private BigDecimal normalisePaymentAmount(
            BigDecimal amount
    ) {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "Payment amount is required."
            );
        }

        BigDecimal normalized = amount.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING
        );

        if (normalized.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }

        return normalized;
    }

    private String normalizeReference(
            String reference,
            String fieldName
    ) {
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        return reference.trim()
                .toUpperCase(Locale.ROOT);
    }

    private void validateStaffId(long staffId) {
        if (staffId <= 0) {
            throw new IllegalArgumentException(
                    "A valid staff user is required."
            );
        }
    }

    private String generateReference(String prefix) {
        String datePart = LocalDate.now()
                .toString()
                .replace("-", "");

        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase(Locale.ROOT);

        return prefix
                + "-"
                + datePart
                + "-"
                + randomPart;
    }
}
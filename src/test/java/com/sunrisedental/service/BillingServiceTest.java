package com.sunrisedental.service;

import com.sunrisedental.dao.BillDao;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.BillPayment;
import com.sunrisedental.model.BillingSource;
import com.sunrisedental.model.PaymentMethod;
import com.sunrisedental.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BillingServiceTest {

    private static final String APPOINTMENT_NUMBER =
            "APT-20260905-ABC123";

    private FakeBillDao billDao;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billDao = new FakeBillDao();

        billDao.sources.put(
                APPOINTMENT_NUMBER,
                new BillingSource(
                        25,
                        APPOINTMENT_NUMBER,
                        AppointmentStatus.COMPLETED,
                        new BigDecimal("2500.00"),
                        new BigDecimal("5000.00")
                )
        );

        billingService = new BillingService(billDao);
    }

    @Test
    void shouldCalculateBillFromDatabaseCharges() {
        Bill bill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        assertEquals(
                new BigDecimal("2500.00"),
                bill.consultationFee()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                bill.treatmentTotal()
        );

        assertEquals(
                new BigDecimal("7500.00"),
                bill.totalAmount()
        );

        assertEquals(
                new BigDecimal("0.00"),
                bill.amountPaid()
        );

        assertEquals(
                PaymentStatus.UNPAID,
                bill.paymentStatus()
        );

        assertTrue(
                bill.billNumber().startsWith("INV-")
        );
    }

    @Test
    void shouldApplyValidDiscount() {
        Bill bill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                new BigDecimal("500.00"),
                7
        );

        assertEquals(
                new BigDecimal("500.00"),
                bill.discountAmount()
        );

        assertEquals(
                new BigDecimal("7000.00"),
                bill.totalAmount()
        );
    }

    @Test
    void shouldMarkFullyDiscountedBillAsPaid() {
        Bill bill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                new BigDecimal("7500.00"),
                7
        );

        assertEquals(
                new BigDecimal("0.00"),
                bill.totalAmount()
        );

        assertEquals(
                PaymentStatus.PAID,
                bill.paymentStatus()
        );
    }

    @Test
    void shouldRejectBillingForScheduledAppointment() {
        billDao.sources.put(
                APPOINTMENT_NUMBER,
                new BillingSource(
                        25,
                        APPOINTMENT_NUMBER,
                        AppointmentStatus.SCHEDULED,
                        new BigDecimal("2500.00"),
                        new BigDecimal("5000.00")
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> billingService.generateBill(
                                APPOINTMENT_NUMBER,
                                BigDecimal.ZERO,
                                7
                        )
                );

        assertEquals(
                "Only completed appointments can be billed.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnknownAppointment() {
        assertThrows(
                IllegalArgumentException.class,
                () -> billingService.generateBill(
                        "APT-UNKNOWN",
                        BigDecimal.ZERO,
                        7
                )
        );
    }

    @Test
    void shouldPreventDuplicateBillForAppointment() {
        billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> billingService.generateBill(
                        APPOINTMENT_NUMBER,
                        BigDecimal.ZERO,
                        7
                )
        );
    }

    @Test
    void shouldRejectDiscountAboveSubtotal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> billingService.generateBill(
                        APPOINTMENT_NUMBER,
                        new BigDecimal("7500.01"),
                        7
                )
        );
    }

    @Test
    void shouldRecordPartialPayment() {
        Bill createdBill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        Bill updatedBill = billingService.recordPayment(
                createdBill.billNumber(),
                new BigDecimal("3000.00"),
                PaymentMethod.CASH,
                9
        );

        assertEquals(
                new BigDecimal("3000.00"),
                updatedBill.amountPaid()
        );

        assertEquals(
                new BigDecimal("4500.00"),
                updatedBill.outstandingAmount()
        );

        assertEquals(
                PaymentStatus.PARTIALLY_PAID,
                updatedBill.paymentStatus()
        );

        assertEquals(
                1,
                billingService.getPayments(
                        updatedBill.billId()
                ).size()
        );
    }

    @Test
    void shouldMarkBillPaidAfterRemainingPayment() {
        Bill createdBill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        billingService.recordPayment(
                createdBill.billNumber(),
                new BigDecimal("3000.00"),
                PaymentMethod.CARD,
                9
        );

        Bill paidBill = billingService.recordPayment(
                createdBill.billNumber(),
                new BigDecimal("4500.00"),
                PaymentMethod.BANK_TRANSFER,
                9
        );

        assertEquals(
                new BigDecimal("7500.00"),
                paidBill.amountPaid()
        );

        assertEquals(
                new BigDecimal("0.00"),
                paidBill.outstandingAmount()
        );

        assertEquals(
                PaymentStatus.PAID,
                paidBill.paymentStatus()
        );

        assertEquals(
                2,
                billingService.getPayments(
                        paidBill.billId()
                ).size()
        );
    }

    @Test
    void shouldRejectPaymentAboveOutstandingBalance() {
        Bill bill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> billingService.recordPayment(
                        bill.billNumber(),
                        new BigDecimal("7500.01"),
                        PaymentMethod.CASH,
                        9
                )
        );
    }

    @Test
    void shouldRejectZeroPayment() {
        Bill bill = billingService.generateBill(
                APPOINTMENT_NUMBER,
                BigDecimal.ZERO,
                7
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> billingService.recordPayment(
                        bill.billNumber(),
                        BigDecimal.ZERO,
                        PaymentMethod.CASH,
                        9
                )
        );
    }

    private static class FakeBillDao implements BillDao {

        private final Map<String, BillingSource> sources =
                new LinkedHashMap<>();

        private final Map<String, Bill> bills =
                new LinkedHashMap<>();

        private final Map<Long, List<BillPayment>> payments =
                new LinkedHashMap<>();

        private long nextBillId = 1;
        private long nextPaymentId = 1;

        @Override
        public Optional<BillingSource>
        findBillingSourceByAppointmentNumber(
                String appointmentNumber
        ) {
            return Optional.ofNullable(
                    sources.get(appointmentNumber)
            );
        }

        @Override
        public boolean existsByAppointmentId(
                long appointmentId
        ) {
            return bills.values()
                    .stream()
                    .anyMatch(
                            bill -> bill.appointmentId()
                                    == appointmentId
                    );
        }

        @Override
        public long create(Bill bill) {
            long billId = nextBillId++;
            LocalDateTime now = LocalDateTime.now();

            Bill savedBill = new Bill(
                    billId,
                    bill.billNumber(),
                    bill.appointmentId(),
                    bill.consultationFee(),
                    bill.treatmentTotal(),
                    bill.discountAmount(),
                    bill.totalAmount(),
                    bill.amountPaid(),
                    bill.paymentStatus(),
                    bill.issuedBy(),
                    now,
                    now
            );

            bills.put(
                    savedBill.billNumber(),
                    savedBill
            );

            return billId;
        }

        @Override
        public Optional<Bill> findByBillNumber(
                String billNumber
        ) {
            return Optional.ofNullable(
                    bills.get(billNumber)
            );
        }

        @Override
        public Optional<Bill> findByAppointmentNumber(
                String appointmentNumber
        ) {
            BillingSource source =
                    sources.get(appointmentNumber);

            if (source == null) {
                return Optional.empty();
            }

            return bills.values()
                    .stream()
                    .filter(
                            bill -> bill.appointmentId()
                                    == source.appointmentId()
                    )
                    .findFirst();
        }

        @Override
        public List<Bill> findAll() {
            return new ArrayList<>(bills.values());
        }

        @Override
        public void recordPayment(BillPayment payment) {
            Bill currentBill = bills.values()
                    .stream()
                    .filter(
                            bill -> bill.billId()
                                    == payment.billId()
                    )
                    .findFirst()
                    .orElseThrow();

            BigDecimal newAmountPaid =
                    currentBill.amountPaid()
                            .add(payment.amount());

            if (newAmountPaid.compareTo(
                    currentBill.totalAmount()
            ) > 0) {
                throw new IllegalArgumentException(
                        "Payment exceeds the outstanding balance."
                );
            }

            PaymentStatus newStatus =
                    newAmountPaid.compareTo(
                            currentBill.totalAmount()
                    ) == 0
                            ? PaymentStatus.PAID
                            : PaymentStatus.PARTIALLY_PAID;

            Bill updatedBill = new Bill(
                    currentBill.billId(),
                    currentBill.billNumber(),
                    currentBill.appointmentId(),
                    currentBill.consultationFee(),
                    currentBill.treatmentTotal(),
                    currentBill.discountAmount(),
                    currentBill.totalAmount(),
                    newAmountPaid,
                    newStatus,
                    currentBill.issuedBy(),
                    currentBill.issuedAt(),
                    LocalDateTime.now()
            );

            bills.put(
                    updatedBill.billNumber(),
                    updatedBill
            );

            BillPayment savedPayment =
                    new BillPayment(
                            nextPaymentId++,
                            payment.receiptNumber(),
                            payment.billId(),
                            payment.amount(),
                            payment.paymentMethod(),
                            payment.receivedBy(),
                            LocalDateTime.now()
                    );

            payments.computeIfAbsent(
                    payment.billId(),
                    ignored -> new ArrayList<>()
            ).add(savedPayment);
        }

        @Override
        public List<BillPayment>
        findPaymentsByBillId(long billId) {
            return new ArrayList<>(
                    payments.getOrDefault(
                            billId,
                            List.of()
                    )
            );
        }
    }
}
package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.BillDao;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.BillPayment;
import com.sunrisedental.model.BillingSource;
import com.sunrisedental.model.PaymentMethod;
import com.sunrisedental.model.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBillDao implements BillDao {

    private static final String FIND_BILLING_SOURCE =
            "SELECT " +
                    "a.appointment_id, " +
                    "a.appointment_number, " +
                    "a.status, " +
                    "d.consultation_fee, " +
                    "COALESCE(SUM(at.quantity * at.charged_fee), 0.00) " +
                    "AS treatment_total " +
                    "FROM appointments a " +
                    "JOIN dentists d " +
                    "ON d.dentist_id = a.dentist_id " +
                    "LEFT JOIN appointment_treatments at " +
                    "ON at.appointment_id = a.appointment_id " +
                    "WHERE a.appointment_number = ? " +
                    "GROUP BY " +
                    "a.appointment_id, " +
                    "a.appointment_number, " +
                    "a.status, " +
                    "d.consultation_fee";

    private static final String CHECK_EXISTING_BILL =
            "SELECT 1 FROM bills " +
                    "WHERE appointment_id = ? LIMIT 1";

    private static final String INSERT_BILL =
            "INSERT INTO bills (" +
                    "bill_number, " +
                    "appointment_id, " +
                    "consultation_fee, " +
                    "treatment_total, " +
                    "discount_amount, " +
                    "total_amount, " +
                    "amount_paid, " +
                    "payment_status, " +
                    "issued_by" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String BILL_COLUMNS =
            "SELECT " +
                    "b.bill_id, " +
                    "b.bill_number, " +
                    "b.appointment_id, " +
                    "b.consultation_fee, " +
                    "b.treatment_total, " +
                    "b.discount_amount, " +
                    "b.total_amount, " +
                    "b.amount_paid, " +
                    "b.payment_status, " +
                    "b.issued_by, " +
                    "b.issued_at, " +
                    "b.updated_at ";

    private static final String FIND_BY_BILL_NUMBER =
            BILL_COLUMNS +
                    "FROM bills b " +
                    "WHERE b.bill_number = ?";

    private static final String FIND_BY_APPOINTMENT_NUMBER =
            BILL_COLUMNS +
                    "FROM bills b " +
                    "JOIN appointments a " +
                    "ON a.appointment_id = b.appointment_id " +
                    "WHERE a.appointment_number = ?";

    private static final String FIND_ALL =
            BILL_COLUMNS +
                    "FROM bills b " +
                    "ORDER BY b.issued_at DESC, b.bill_id DESC";

    private static final String LOCK_BILL_FOR_PAYMENT =
            "SELECT total_amount, amount_paid " +
                    "FROM bills " +
                    "WHERE bill_id = ? " +
                    "FOR UPDATE";

    private static final String INSERT_PAYMENT =
            "INSERT INTO bill_payments (" +
                    "receipt_number, " +
                    "bill_id, " +
                    "amount, " +
                    "payment_method, " +
                    "received_by" +
                    ") VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_PAYMENT_PROGRESS =
            "UPDATE bills " +
                    "SET amount_paid = ?, payment_status = ? " +
                    "WHERE bill_id = ?";

    private static final String FIND_PAYMENTS =
            "SELECT " +
                    "payment_id, " +
                    "receipt_number, " +
                    "bill_id, " +
                    "amount, " +
                    "payment_method, " +
                    "received_by, " +
                    "paid_at " +
                    "FROM bill_payments " +
                    "WHERE bill_id = ? " +
                    "ORDER BY paid_at ASC, payment_id ASC";

    @Override
    public Optional<BillingSource>
    findBillingSourceByAppointmentNumber(
            String appointmentNumber
    ) {
        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_BILLING_SOURCE
                        )
        ) {
            statement.setString(1, appointmentNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        new BillingSource(
                                resultSet.getLong(
                                        "appointment_id"
                                ),
                                resultSet.getString(
                                        "appointment_number"
                                ),
                                AppointmentStatus.valueOf(
                                        resultSet.getString("status")
                                ),
                                resultSet.getBigDecimal(
                                        "consultation_fee"
                                ),
                                resultSet.getBigDecimal(
                                        "treatment_total"
                                )
                        )
                );
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to load appointment billing data.",
                    exception
            );
        }
    }

    @Override
    public boolean existsByAppointmentId(long appointmentId) {
        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                CHECK_EXISTING_BILL
                        )
        ) {
            statement.setLong(1, appointmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to check for an existing bill.",
                    exception
            );
        }
    }

    @Override
    public long create(Bill bill) {
        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_BILL,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, bill.billNumber());
            statement.setLong(2, bill.appointmentId());
            statement.setBigDecimal(
                    3,
                    bill.consultationFee()
            );
            statement.setBigDecimal(
                    4,
                    bill.treatmentTotal()
            );
            statement.setBigDecimal(
                    5,
                    bill.discountAmount()
            );
            statement.setBigDecimal(
                    6,
                    bill.totalAmount()
            );
            statement.setBigDecimal(
                    7,
                    bill.amountPaid()
            );
            statement.setString(
                    8,
                    bill.paymentStatus().name()
            );
            statement.setLong(9, bill.issuedBy());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new IllegalStateException(
                        "The bill could not be created."
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new IllegalStateException(
                    "The generated bill ID was not returned."
            );
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to create the bill.",
                    exception
            );
        }
    }

    @Override
    public Optional<Bill> findByBillNumber(
            String billNumber
    ) {
        return findSingleBill(
                FIND_BY_BILL_NUMBER,
                billNumber
        );
    }

    @Override
    public Optional<Bill> findByAppointmentNumber(
            String appointmentNumber
    ) {
        return findSingleBill(
                FIND_BY_APPOINTMENT_NUMBER,
                appointmentNumber
        );
    }

    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_ALL);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            while (resultSet.next()) {
                bills.add(mapBill(resultSet));
            }

            return bills;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to load the billing directory.",
                    exception
            );
        }
    }

    @Override
    public void recordPayment(BillPayment payment) {
        Connection connection = null;

        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);

            BigDecimal totalAmount;
            BigDecimal currentAmountPaid;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    LOCK_BILL_FOR_PAYMENT
                            )
            ) {
                statement.setLong(1, payment.billId());

                try (
                        ResultSet resultSet =
                                statement.executeQuery()
                ) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException(
                                "The selected bill does not exist."
                        );
                    }

                    totalAmount = resultSet.getBigDecimal(
                            "total_amount"
                    );
                    currentAmountPaid =
                            resultSet.getBigDecimal(
                                    "amount_paid"
                            );
                }
            }

            if (payment.amount() == null
                    || payment.amount().signum() <= 0) {
                throw new IllegalArgumentException(
                        "Payment amount must be greater than zero."
                );
            }

            BigDecimal newAmountPaid =
                    currentAmountPaid.add(payment.amount());

            if (newAmountPaid.compareTo(totalAmount) > 0) {
                throw new IllegalArgumentException(
                        "Payment exceeds the outstanding balance."
                );
            }

            PaymentStatus newStatus =
                    newAmountPaid.compareTo(totalAmount) == 0
                            ? PaymentStatus.PAID
                            : PaymentStatus.PARTIALLY_PAID;

            insertPayment(connection, payment);

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    UPDATE_PAYMENT_PROGRESS
                            )
            ) {
                statement.setBigDecimal(1, newAmountPaid);
                statement.setString(
                        2,
                        newStatus.name()
                );
                statement.setLong(3, payment.billId());

                if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException(
                            "The bill payment status " +
                                    "could not be updated."
                    );
                }
            }

            connection.commit();
        } catch (SQLException exception) {
            rollbackQuietly(connection);

            throw new IllegalStateException(
                    "Unable to record the payment.",
                    exception
            );
        } catch (RuntimeException exception) {
            rollbackQuietly(connection);
            throw exception;
        } finally {
            closeQuietly(connection);
        }
    }

    @Override
    public List<BillPayment> findPaymentsByBillId(
            long billId
    ) {
        List<BillPayment> payments = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                FIND_PAYMENTS
                        )
        ) {
            statement.setLong(1, billId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapPayment(resultSet));
                }
            }

            return payments;
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to load payment history.",
                    exception
            );
        }
    }

    private Optional<Bill> findSingleBill(
            String sql,
            String searchValue
    ) {
        try (
                Connection connection =
                        DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, searchValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapBill(resultSet));
                }

                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to load the bill.",
                    exception
            );
        }
    }

    private void insertPayment(
            Connection connection,
            BillPayment payment
    ) throws SQLException {
        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_PAYMENT
                        )
        ) {
            statement.setString(
                    1,
                    payment.receiptNumber()
            );
            statement.setLong(2, payment.billId());
            statement.setBigDecimal(3, payment.amount());
            statement.setString(
                    4,
                    payment.paymentMethod().name()
            );
            statement.setLong(5, payment.receivedBy());

            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException(
                        "The payment record could not be created."
                );
            }
        }
    }

    private Bill mapBill(ResultSet resultSet)
            throws SQLException {
        return new Bill(
                resultSet.getLong("bill_id"),
                resultSet.getString("bill_number"),
                resultSet.getLong("appointment_id"),
                resultSet.getBigDecimal(
                        "consultation_fee"
                ),
                resultSet.getBigDecimal(
                        "treatment_total"
                ),
                resultSet.getBigDecimal(
                        "discount_amount"
                ),
                resultSet.getBigDecimal(
                        "total_amount"
                ),
                resultSet.getBigDecimal(
                        "amount_paid"
                ),
                PaymentStatus.valueOf(
                        resultSet.getString("payment_status")
                ),
                resultSet.getLong("issued_by"),
                toLocalDateTime(
                        resultSet.getTimestamp("issued_at")
                ),
                toLocalDateTime(
                        resultSet.getTimestamp("updated_at")
                )
        );
    }

    private BillPayment mapPayment(ResultSet resultSet)
            throws SQLException {
        return new BillPayment(
                resultSet.getLong("payment_id"),
                resultSet.getString("receipt_number"),
                resultSet.getLong("bill_id"),
                resultSet.getBigDecimal("amount"),
                PaymentMethod.valueOf(
                        resultSet.getString("payment_method")
                ),
                resultSet.getLong("received_by"),
                toLocalDateTime(
                        resultSet.getTimestamp("paid_at")
                )
        );
    }

    private java.time.LocalDateTime toLocalDateTime(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    private void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original exception.
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ignored) {
            // Nothing further can be done safely.
        }
    }
}
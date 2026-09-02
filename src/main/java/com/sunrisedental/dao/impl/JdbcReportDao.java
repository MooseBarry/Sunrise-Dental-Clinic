package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.ReportDao;
import com.sunrisedental.model.DailyRevenue;
import com.sunrisedental.model.ManagementReport;
import com.sunrisedental.model.ReportActivityRow;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcReportDao implements ReportDao {
    @Override
    public ManagementReport generate(
            LocalDate fromDate,
            LocalDate toDate
    ) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            long[] appointments = appointmentCounts(
                    connection, fromDate, toDate
            );
            long patients = newPatientCount(connection, fromDate, toDate);
            BigDecimal[] money = billingTotals(
                    connection, fromDate, toDate
            );
            return new ManagementReport(
                    fromDate,
                    toDate,
                    appointments[0],
                    appointments[1],
                    appointments[2],
                    appointments[3],
                    appointments[4],
                    patients,
                    money[0],
                    money[1],
                    money[2],
                    treatmentActivity(connection, fromDate, toDate),
                    dentistActivity(connection, fromDate, toDate),
                    dailyRevenue(connection, fromDate, toDate)
            );
        }
    }

    private long[] appointmentCounts(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT COUNT(*) total_count, "
                + "SUM(status = 'SCHEDULED') scheduled_count, "
                + "SUM(status = 'COMPLETED') completed_count, "
                + "SUM(status = 'CANCELLED') cancelled_count, "
                + "SUM(status = 'NO_SHOW') no_show_count "
                + "FROM appointments WHERE appointment_date BETWEEN ? AND ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDates(statement, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new long[]{
                        resultSet.getLong("total_count"),
                        resultSet.getLong("scheduled_count"),
                        resultSet.getLong("completed_count"),
                        resultSet.getLong("cancelled_count"),
                        resultSet.getLong("no_show_count")
                };
            }
        }
    }

    private long newPatientCount(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients "
                + "WHERE DATE(created_at) BETWEEN ? AND ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDates(statement, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private BigDecimal[] billingTotals(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT COALESCE(SUM(b.total_amount), 0) invoiced, "
                + "COALESCE(SUM(b.amount_paid), 0) received, "
                + "COALESCE(SUM(b.total_amount - b.amount_paid), 0) outstanding "
                + "FROM bills b JOIN appointments a "
                + "ON a.appointment_id = b.appointment_id "
                + "WHERE a.appointment_date BETWEEN ? AND ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDates(statement, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new BigDecimal[]{
                        resultSet.getBigDecimal("invoiced"),
                        resultSet.getBigDecimal("received"),
                        resultSet.getBigDecimal("outstanding")
                };
            }
        }
    }

    private List<ReportActivityRow> treatmentActivity(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT t.treatment_name label, "
                + "SUM(at.quantity) activity_count, "
                + "SUM(at.quantity * at.charged_fee) amount "
                + "FROM appointment_treatments at "
                + "JOIN treatments t ON t.treatment_id = at.treatment_id "
                + "JOIN appointments a ON a.appointment_id = at.appointment_id "
                + "WHERE a.appointment_date BETWEEN ? AND ? "
                + "GROUP BY t.treatment_id, t.treatment_name "
                + "ORDER BY activity_count DESC, label LIMIT 10";
        return activityRows(connection, sql, from, to);
    }

    private List<ReportActivityRow> dentistActivity(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT u.full_name label, COUNT(a.appointment_id) "
                + "activity_count, COALESCE(SUM(b.total_amount), 0) amount "
                + "FROM dentists d JOIN users u ON u.user_id = d.user_id "
                + "LEFT JOIN appointments a ON a.dentist_id = d.dentist_id "
                + "AND a.appointment_date BETWEEN ? AND ? "
                + "LEFT JOIN bills b ON b.appointment_id = a.appointment_id "
                + "GROUP BY d.dentist_id, u.full_name "
                + "ORDER BY activity_count DESC, label";
        return activityRows(connection, sql, from, to);
    }

    private List<ReportActivityRow> activityRows(
            Connection connection,
            String sql,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        List<ReportActivityRow> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDates(statement, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new ReportActivityRow(
                            resultSet.getString("label"),
                            resultSet.getLong("activity_count"),
                            resultSet.getBigDecimal("amount")
                    ));
                }
            }
        }
        return rows;
    }

    private List<DailyRevenue> dailyRevenue(
            Connection connection,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        String sql = "SELECT DATE(paid_at) revenue_date, SUM(amount) amount "
                + "FROM bill_payments WHERE DATE(paid_at) BETWEEN ? AND ? "
                + "GROUP BY DATE(paid_at) ORDER BY revenue_date";
        List<DailyRevenue> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDates(statement, from, to);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new DailyRevenue(
                            resultSet.getDate("revenue_date").toLocalDate(),
                            resultSet.getBigDecimal("amount")
                    ));
                }
            }
        }
        return rows;
    }

    private void bindDates(
            PreparedStatement statement,
            LocalDate from,
            LocalDate to
    ) throws SQLException {
        statement.setDate(1, Date.valueOf(from));
        statement.setDate(2, Date.valueOf(to));
    }
}

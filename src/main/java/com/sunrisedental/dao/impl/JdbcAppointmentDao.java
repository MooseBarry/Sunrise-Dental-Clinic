package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;

public class JdbcAppointmentDao implements AppointmentDao {

    private static final String INSERT_APPOINTMENT =
            "INSERT INTO appointments (" +
                    "appointment_number, patient_id, dentist_id, " +
                    "appointment_date, start_time, duration_minutes, " +
                    "reason, status, notes, created_by" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_TREATMENT =
            "INSERT INTO appointment_treatments (" +
                    "appointment_id, treatment_id, quantity, charged_fee" +
                    ") " +
                    "SELECT ?, treatment_id, 1, standard_fee " +
                    "FROM treatments " +
                    "WHERE treatment_id = ? AND active = TRUE";

    private static final String CHECK_DENTIST_SLOT =
            "SELECT 1 FROM appointments " +
                    "WHERE dentist_id = ? " +
                    "AND appointment_date = ? " +
                    "AND start_time = ? " +
                    "LIMIT 1";

    @Override
    public long create(Appointment appointment)
            throws SQLException {

        try (Connection connection =
                     DatabaseConfig.getConnection()) {

            boolean originalAutoCommit =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                long appointmentId =
                        insertAppointment(
                                connection,
                                appointment
                        );

                insertTreatment(
                        connection,
                        appointmentId,
                        appointment.treatmentId()
                );

                connection.commit();
                return appointmentId;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;

            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    @Override
    public boolean existsDentistSlot(
            long dentistId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                CHECK_DENTIST_SLOT
                        )
        ) {
            statement.setLong(1, dentistId);
            statement.setDate(
                    2,
                    Date.valueOf(appointmentDate)
            );
            statement.setTime(
                    3,
                    Time.valueOf(startTime)
            );

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private long insertAppointment(
            Connection connection,
            Appointment appointment
    ) throws SQLException {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_APPOINTMENT,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(
                    1,
                    appointment.appointmentNumber()
            );
            statement.setLong(2, appointment.patientId());
            statement.setLong(3, appointment.dentistId());
            statement.setDate(
                    4,
                    Date.valueOf(appointment.appointmentDate())
            );
            statement.setTime(
                    5,
                    Time.valueOf(appointment.startTime())
            );
            statement.setInt(
                    6,
                    appointment.durationMinutes()
            );

            setNullableString(
                    statement,
                    7,
                    appointment.reason()
            );

            statement.setString(
                    8,
                    appointment.status().name()
            );

            setNullableString(
                    statement,
                    9,
                    appointment.notes()
            );

            statement.setLong(10, appointment.createdBy());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Appointment was not created."
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException(
                    "Appointment ID was not generated."
            );
        }
    }

    private void insertTreatment(
            Connection connection,
            long appointmentId,
            long treatmentId
    ) throws SQLException {

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_TREATMENT
                        )
        ) {
            statement.setLong(1, appointmentId);
            statement.setLong(2, treatmentId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "The selected treatment is unavailable."
                );
            }
        }
    }

    private void setNullableString(
            PreparedStatement statement,
            int index,
            String value
    ) throws SQLException {

        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }
}
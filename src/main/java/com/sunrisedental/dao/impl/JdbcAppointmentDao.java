package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.AppointmentStatus;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private static final String SELECT_DETAILS =
            "SELECT " +
                    "a.appointment_id, a.appointment_number, " +
                    "p.patient_number, " +
                    "CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                    "p.address AS patient_address, " +
                    "p.contact_number AS patient_contact, " +
                    "dentist_user.full_name AS dentist_name, " +
                    "d.registration_number, " +
                    "t.treatment_name, at.quantity, at.charged_fee, " +
                    "a.appointment_date, a.start_time, " +
                    "a.duration_minutes, a.reason, a.status, a.notes, " +
                    "creator.full_name AS created_by_name, a.created_at " +
                    "FROM appointments a " +
                    "INNER JOIN patients p " +
                    "ON a.patient_id = p.patient_id " +
                    "INNER JOIN dentists d " +
                    "ON a.dentist_id = d.dentist_id " +
                    "INNER JOIN users dentist_user " +
                    "ON d.user_id = dentist_user.user_id " +
                    "INNER JOIN appointment_treatments at " +
                    "ON a.appointment_id = at.appointment_id " +
                    "INNER JOIN treatments t " +
                    "ON at.treatment_id = t.treatment_id " +
                    "INNER JOIN users creator " +
                    "ON a.created_by = creator.user_id ";

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
    @Override
    public List<AppointmentDetails> findAll()
            throws SQLException {

        String sql = SELECT_DETAILS
                + "ORDER BY a.appointment_date DESC, "
                + "a.start_time DESC";

        List<AppointmentDetails> appointments =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                appointments.add(
                        mapAppointmentDetails(resultSet)
                );
            }
        }

        return appointments;
    }

    @Override
    public Optional<AppointmentDetails>
    findByAppointmentNumber(
            String appointmentNumber
    ) throws SQLException {

        String sql = SELECT_DETAILS
                + "WHERE a.appointment_number = ? "
                + "LIMIT 1";

        try (
                Connection connection =
                        DatabaseConfig.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, appointmentNumber);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapAppointmentDetails(resultSet)
                    );
                }
            }
        }

        return Optional.empty();
    }

    private AppointmentDetails mapAppointmentDetails(
            ResultSet resultSet
    ) throws SQLException {

        Timestamp createdAt =
                resultSet.getTimestamp("created_at");

        return new AppointmentDetails(
                resultSet.getLong("appointment_id"),
                resultSet.getString("appointment_number"),
                resultSet.getString("patient_number"),
                resultSet.getString("patient_name"),
                resultSet.getString("patient_address"),
                resultSet.getString("patient_contact"),
                resultSet.getString("dentist_name"),
                resultSet.getString("registration_number"),
                resultSet.getString("treatment_name"),
                resultSet.getInt("quantity"),
                resultSet.getBigDecimal("charged_fee"),
                resultSet.getDate(
                        "appointment_date"
                ).toLocalDate(),
                resultSet.getTime(
                        "start_time"
                ).toLocalTime(),
                resultSet.getInt("duration_minutes"),
                resultSet.getString("reason"),
                AppointmentStatus.valueOf(
                        resultSet.getString("status")
                ),
                resultSet.getString("notes"),
                resultSet.getString("created_by_name"),
                createdAt.toLocalDateTime()
        );
    }
}
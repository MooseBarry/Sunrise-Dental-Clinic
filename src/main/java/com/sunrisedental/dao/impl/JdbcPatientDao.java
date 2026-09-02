package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.model.Patient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPatientDao implements PatientDao {

    private static final String INSERT_SQL =
            "INSERT INTO patients (" +
                    "patient_number, first_name, last_name, " +
                    "date_of_birth, gender, nic_number, " +
                    "contact_number, email, address, medical_notes" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_COLUMNS =
            "SELECT patient_id, patient_number, first_name, " +
                    "last_name, date_of_birth, gender, nic_number, " +
                    "contact_number, email, address, medical_notes " +
                    "FROM patients ";

    @Override
    public long create(Patient patient) throws SQLException {
        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                INSERT_SQL,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, patient.patientNumber());
            statement.setString(2, patient.firstName());
            statement.setString(3, patient.lastName());

            if (patient.dateOfBirth() == null) {
                statement.setNull(4, Types.DATE);
            } else {
                statement.setDate(
                        4,
                        Date.valueOf(patient.dateOfBirth())
                );
            }

            setNullableString(statement, 5, patient.gender());
            setNullableString(statement, 6, patient.nicNumber());
            statement.setString(7, patient.contactNumber());
            setNullableString(statement, 8, patient.email());
            setNullableString(statement, 9, patient.address());
            setNullableString(
                    statement,
                    10,
                    patient.medicalNotes()
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new SQLException(
                        "Patient record was not created."
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException(
                    "Patient ID was not generated."
            );
        }
    }

    @Override
    public List<Patient> findAll() throws SQLException {
        String sql = SELECT_COLUMNS
                + "ORDER BY created_at DESC";

        List<Patient> patients = new ArrayList<>();

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }
        }

        return patients;
    }

    @Override
    public List<Patient> search(String query) throws SQLException {
        String sql = SELECT_COLUMNS
                + "WHERE patient_number LIKE ? "
                + "OR first_name LIKE ? OR last_name LIKE ? "
                + "OR contact_number LIKE ? OR nic_number LIKE ? "
                + "ORDER BY created_at DESC";
        List<Patient> patients = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            for (int index = 1; index <= 5; index++) {
                statement.setString(index, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    patients.add(mapPatient(resultSet));
                }
            }
        }
        return patients;
    }

    @Override
    public Optional<Patient> findById(long patientId)
            throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE patient_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapPatient(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Patient> findByPatientNumber(
            String patientNumber
    ) throws SQLException {

        String sql = SELECT_COLUMNS
                + "WHERE patient_number = ?";

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, patientNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapPatient(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, "
                + "date_of_birth = ?, gender = ?, nic_number = ?, "
                + "contact_number = ?, email = ?, address = ?, "
                + "medical_notes = ? WHERE patient_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.firstName());
            statement.setString(2, patient.lastName());
            if (patient.dateOfBirth() == null) {
                statement.setNull(3, Types.DATE);
            } else {
                statement.setDate(3, Date.valueOf(patient.dateOfBirth()));
            }
            setNullableString(statement, 4, patient.gender());
            setNullableString(statement, 5, patient.nicNumber());
            statement.setString(6, patient.contactNumber());
            setNullableString(statement, 7, patient.email());
            setNullableString(statement, 8, patient.address());
            setNullableString(statement, 9, patient.medicalNotes());
            statement.setLong(10, patient.patientId());
            return statement.executeUpdate() == 1;
        }
    }

    private Patient mapPatient(ResultSet resultSet)
            throws SQLException {

        Date sqlDate = resultSet.getDate("date_of_birth");

        LocalDate dateOfBirth =
                sqlDate == null ? null : sqlDate.toLocalDate();

        return new Patient(
                resultSet.getLong("patient_id"),
                resultSet.getString("patient_number"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                dateOfBirth,
                resultSet.getString("gender"),
                resultSet.getString("nic_number"),
                resultSet.getString("contact_number"),
                resultSet.getString("email"),
                resultSet.getString("address"),
                resultSet.getString("medical_notes")
        );
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

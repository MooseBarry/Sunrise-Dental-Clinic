package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.TreatmentDao;
import com.sunrisedental.model.Treatment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTreatmentDao implements TreatmentDao {
    private static final String COLUMNS =
            "treatment_id, treatment_code, treatment_name, "
                    + "description, standard_fee, active";

    @Override
    public List<Treatment> findAll() throws SQLException {
        List<Treatment> treatments = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM treatments "
                             + "ORDER BY active DESC, treatment_name"
             );
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                treatments.add(map(resultSet));
            }
        }
        return treatments;
    }

    @Override
    public Optional<Treatment> findById(long treatmentId)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS
                             + " FROM treatments WHERE treatment_id = ?"
             )) {
            statement.setLong(1, treatmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(map(resultSet))
                        : Optional.empty();
            }
        }
    }

    @Override
    public boolean codeExists(String treatmentCode) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM treatments "
                             + "WHERE treatment_code = ? LIMIT 1"
             )) {
            statement.setString(1, treatmentCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public long create(Treatment treatment) throws SQLException {
        String sql = "INSERT INTO treatments (treatment_code, "
                + "treatment_name, description, standard_fee, active) "
                + "VALUES (?, ?, ?, ?, TRUE)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {
            bind(statement, treatment);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Treatment was not created.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new SQLException("Treatment ID was not returned.");
        }
    }

    @Override
    public boolean update(Treatment treatment) throws SQLException {
        String sql = "UPDATE treatments SET treatment_code = ?, "
                + "treatment_name = ?, description = ?, standard_fee = ? "
                + "WHERE treatment_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, treatment);
            statement.setLong(5, treatment.treatmentId());
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean updateActive(long treatmentId, boolean active)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE treatments SET active = ? "
                             + "WHERE treatment_id = ?"
             )) {
            statement.setBoolean(1, active);
            statement.setLong(2, treatmentId);
            return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, Treatment treatment)
            throws SQLException {
        statement.setString(1, treatment.treatmentCode());
        statement.setString(2, treatment.treatmentName());
        if (treatment.description() == null) {
            statement.setNull(3, Types.VARCHAR);
        } else {
            statement.setString(3, treatment.description());
        }
        statement.setBigDecimal(4, treatment.standardFee());
    }

    private Treatment map(ResultSet resultSet) throws SQLException {
        return new Treatment(
                resultSet.getLong("treatment_id"),
                resultSet.getString("treatment_code"),
                resultSet.getString("treatment_name"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("standard_fee"),
                resultSet.getBoolean("active")
        );
    }
}

package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.StaffDao;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.StaffAccount;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcStaffDao implements StaffDao {
    private static final String SELECT_ALL =
            "SELECT u.user_id, u.username, u.full_name, u.email, "
                    + "u.contact_number, r.role_name, u.active, "
                    + "u.created_at FROM users u JOIN roles r "
                    + "ON r.role_id = u.role_id "
                    + "ORDER BY u.active DESC, u.full_name";

    @Override
    public List<StaffAccount> findAll() throws SQLException {
        List<StaffAccount> accounts = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                accounts.add(new StaffAccount(
                        resultSet.getLong("user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("contact_number"),
                        Role.valueOf(resultSet.getString("role_name")),
                        resultSet.getBoolean("active"),
                        resultSet.getTimestamp("created_at")
                                .toLocalDateTime()
                ));
            }
        }
        return accounts;
    }

    @Override
    public boolean usernameExists(String username)
            throws SQLException {
        return exists("username", username);
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        return exists("email", email);
    }

    private boolean exists(String column, String value)
            throws SQLException {
        String sql = "SELECT 1 FROM users WHERE " + column
                + " = ? LIMIT 1";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public long create(
            User user,
            String registrationNumber,
            String specialization,
            BigDecimal consultationFee
    ) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, "
                + "full_name, email, contact_number, role_id, active) "
                + "SELECT ?, ?, ?, ?, ?, role_id, TRUE FROM roles "
                + "WHERE role_name = ?";
        try (Connection connection = DatabaseConfig.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     sql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {
                    statement.setString(1, user.getUsername());
                    statement.setString(2, user.getPasswordHash());
                    statement.setString(3, user.getFullName());
                    setNullable(statement, 4, user.getEmail());
                    setNullable(statement, 5, user.getContactNumber());
                    statement.setString(6, user.getRole().name());

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                                "Staff account was not created."
                        );
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Staff account ID was not returned."
                            );
                        }
                        userId = keys.getLong(1);
                    }
                }

                if (user.getRole() == Role.DENTIST) {
                    insertDentistProfile(
                            connection,
                            userId,
                            registrationNumber,
                            specialization,
                            consultationFee
                    );
                }

                connection.commit();
                return userId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void insertDentistProfile(
            Connection connection,
            long userId,
            String registrationNumber,
            String specialization,
            BigDecimal consultationFee
    ) throws SQLException {
        String sql = "INSERT INTO dentists (user_id, registration_number, "
                + "specialization, consultation_fee, active) "
                + "VALUES (?, ?, ?, ?, TRUE)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, registrationNumber);
            setNullable(statement, 3, specialization);
            statement.setBigDecimal(4, consultationFee);
            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Dentist professional profile was not created."
                );
            }
        }
    }

    @Override
    public boolean updateActive(long userId, boolean active)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET active = ? WHERE user_id = ?"
             )) {
            statement.setBoolean(1, active);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean updatePassword(long userId, String passwordHash)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET password_hash = ? WHERE user_id = ?"
             )) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    private void setNullable(
            PreparedStatement statement,
            int index,
            String value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }
}

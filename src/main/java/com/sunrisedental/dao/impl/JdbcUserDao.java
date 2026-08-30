package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.UserDao;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String FIND_BY_USERNAME_SQL =
            "SELECT u.user_id, u.username, u.password_hash, " +
                    "u.full_name, u.email, u.contact_number, " +
                    "u.active, u.created_at, r.role_name " +
                    "FROM users u " +
                    "INNER JOIN roles r ON u.role_id = r.role_id " +
                    "WHERE LOWER(u.username) = LOWER(?) " +
                    "LIMIT 1";

    @Override
    public Optional<User> findByUsername(String username)
            throws SQLException {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(FIND_BY_USERNAME_SQL)
        ) {
            statement.setString(1, username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(resultSet));
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role;

        try {
            role = Role.valueOf(
                    resultSet.getString("role_name")
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new SQLException(
                    "Unknown role found for user.",
                    exception
            );
        }

        Timestamp createdAt = resultSet.getTimestamp("created_at");

        return new User(
                resultSet.getLong("user_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("contact_number"),
                role,
                resultSet.getBoolean("active"),
                createdAt.toLocalDateTime()
        );
    }
}
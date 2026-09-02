package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.NotificationDao;
import com.sunrisedental.model.NotificationType;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.StaffNotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JdbcNotificationDao implements NotificationDao {
    @Override
    public List<StaffNotification> findForUser(long userId)
            throws SQLException {
        String sql = "SELECT notification_id, recipient_user_id, "
                + "notification_type, title, message, reference_type, "
                + "reference_value, read_at, created_at "
                + "FROM staff_notifications WHERE recipient_user_id = ? "
                + "ORDER BY created_at DESC LIMIT 100";
        List<StaffNotification> notifications = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(map(resultSet));
                }
            }
        }
        return notifications;
    }

    @Override
    public int countUnread(long userId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM staff_notifications "
                             + "WHERE recipient_user_id = ? AND read_at IS NULL"
             )) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    @Override
    public boolean markRead(long notificationId, long userId)
            throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE staff_notifications SET read_at = CURRENT_TIMESTAMP "
                             + "WHERE notification_id = ? "
                             + "AND recipient_user_id = ? AND read_at IS NULL"
             )) {
            statement.setLong(1, notificationId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public int markAllRead(long userId) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE staff_notifications SET read_at = CURRENT_TIMESTAMP "
                             + "WHERE recipient_user_id = ? AND read_at IS NULL"
             )) {
            statement.setLong(1, userId);
            return statement.executeUpdate();
        }
    }

    @Override
    public void createForRoles(
            Set<Role> roles,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            String referenceValue
    ) throws SQLException {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        String placeholders = roles.stream()
                .map(role -> "?")
                .collect(Collectors.joining(","));
        String sql = "INSERT INTO staff_notifications (recipient_user_id, "
                + "notification_type, title, message, reference_type, "
                + "reference_value) SELECT u.user_id, ?, ?, ?, ?, ? "
                + "FROM users u JOIN roles r ON r.role_id = u.role_id "
                + "WHERE u.active = TRUE AND r.role_name IN ("
                + placeholders + ")";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type.name());
            statement.setString(2, title);
            statement.setString(3, message);
            setNullable(statement, 4, referenceType);
            setNullable(statement, 5, referenceValue);
            int index = 6;
            for (Role role : roles) {
                statement.setString(index++, role.name());
            }
            statement.executeUpdate();
        }
    }

    private StaffNotification map(ResultSet resultSet)
            throws SQLException {
        return new StaffNotification(
                resultSet.getLong("notification_id"),
                resultSet.getLong("recipient_user_id"),
                NotificationType.valueOf(
                        resultSet.getString("notification_type")
                ),
                resultSet.getString("title"),
                resultSet.getString("message"),
                resultSet.getString("reference_type"),
                resultSet.getString("reference_value"),
                toDateTime(resultSet.getTimestamp("read_at")),
                toDateTime(resultSet.getTimestamp("created_at"))
        );
    }

    private java.time.LocalDateTime toDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void setNullable(
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

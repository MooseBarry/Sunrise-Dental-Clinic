package com.sunrisedental.service;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.model.AuditEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditService {
    private static final Logger LOGGER =
            Logger.getLogger(AuditService.class.getName());

    public void record(
            Long actorUserId,
            String action,
            String entityType,
            String entityReference,
            String details
    ) {
        String sql = "INSERT INTO audit_logs (actor_user_id, "
                + "action_name, entity_type, entity_reference, details) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            if (actorUserId == null) {
                statement.setNull(1, Types.BIGINT);
            } else {
                statement.setLong(1, actorUserId);
            }
            statement.setString(2, trim(action, 80));
            statement.setString(3, trim(entityType, 40));
            setNullable(statement, 4, trim(entityReference, 60));
            setNullable(statement, 5, trim(details, 500));
            statement.executeUpdate();
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Audit event was not stored.", exception);
        }
    }

    public List<AuditEntry> getRecent(int limit) throws SQLException {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = "SELECT a.audit_id, COALESCE(u.full_name, 'System') "
                + "actor_name, a.action_name, a.entity_type, "
                + "a.entity_reference, a.details, a.created_at "
                + "FROM audit_logs a LEFT JOIN users u "
                + "ON u.user_id = a.actor_user_id "
                + "ORDER BY a.created_at DESC LIMIT ?";
        List<AuditEntry> entries = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, safeLimit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    entries.add(new AuditEntry(
                            resultSet.getLong("audit_id"),
                            resultSet.getString("actor_name"),
                            resultSet.getString("action_name"),
                            resultSet.getString("entity_type"),
                            resultSet.getString("entity_reference"),
                            resultSet.getString("details"),
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime()
                    ));
                }
            }
        }
        return entries;
    }

    private String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() <= max
                ? normalized
                : normalized.substring(0, max);
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

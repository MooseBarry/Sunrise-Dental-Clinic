package com.sunrisedental.service;

import com.sunrisedental.dao.NotificationDao;
import com.sunrisedental.model.NotificationType;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.StaffNotification;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class NotificationService {
    private final NotificationDao notificationDao;

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<StaffNotification> getForUser(long userId)
            throws SQLException {
        validateUserId(userId);
        return notificationDao.findForUser(userId);
    }

    public int countUnread(long userId) throws SQLException {
        validateUserId(userId);
        return notificationDao.countUnread(userId);
    }

    public void markRead(long notificationId, long userId)
            throws SQLException {
        validateUserId(userId);
        if (notificationId <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid notification."
            );
        }
        notificationDao.markRead(notificationId, userId);
    }

    public int markAllRead(long userId) throws SQLException {
        validateUserId(userId);
        return notificationDao.markAllRead(userId);
    }

    public void publish(
            Set<Role> roles,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            String referenceValue
    ) throws SQLException {
        notificationDao.createForRoles(
                roles,
                type,
                trim(title, 120),
                trim(message, 500),
                trim(referenceType, 30),
                trim(referenceValue, 50)
        );
    }

    private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("A valid user is required.");
        }
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
}

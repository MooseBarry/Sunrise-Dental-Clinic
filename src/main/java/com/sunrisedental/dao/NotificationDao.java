package com.sunrisedental.dao;

import com.sunrisedental.model.NotificationType;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.StaffNotification;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public interface NotificationDao {
    List<StaffNotification> findForUser(long userId) throws SQLException;

    int countUnread(long userId) throws SQLException;

    boolean markRead(long notificationId, long userId) throws SQLException;

    int markAllRead(long userId) throws SQLException;

    void createForRoles(
            Set<Role> roles,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            String referenceValue
    ) throws SQLException;
}

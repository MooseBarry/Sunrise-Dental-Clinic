package com.sunrisedental.dao;

import com.sunrisedental.model.StaffAccount;
import com.sunrisedental.model.User;

import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.List;

public interface StaffDao {
    List<StaffAccount> findAll() throws SQLException;

    boolean usernameExists(String username) throws SQLException;

    boolean emailExists(String email) throws SQLException;

    long create(
            User user,
            String registrationNumber,
            String specialization,
            BigDecimal consultationFee
    ) throws SQLException;

    boolean updateActive(long userId, boolean active)
            throws SQLException;

    boolean updatePassword(long userId, String passwordHash)
            throws SQLException;
}

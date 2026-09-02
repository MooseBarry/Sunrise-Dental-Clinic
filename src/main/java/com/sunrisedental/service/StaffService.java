package com.sunrisedental.service;

import com.sunrisedental.dao.StaffDao;
import com.sunrisedental.model.StaffAccount;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

import java.sql.SQLException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public class StaffService {
    private final StaffDao staffDao;

    public StaffService(StaffDao staffDao) {
        if (staffDao == null) {
            throw new IllegalArgumentException("Staff DAO is required.");
        }
        this.staffDao = staffDao;
    }

    public List<StaffAccount> getAll() throws SQLException {
        return staffDao.findAll();
    }

    public long register(StaffRegistrationRequest request)
            throws SQLException {
        if (request == null || request.role() == null) {
            throw new IllegalArgumentException(
                    "Complete all required staff details."
            );
        }

        String username = require(request.username(), "Username", 50)
                .toLowerCase(Locale.ROOT);
        if (!username.matches("[a-z0-9._-]{4,50}")) {
            throw new IllegalArgumentException(
                    "Username must contain 4-50 letters, numbers, dots, dashes or underscores."
            );
        }
        if (staffDao.usernameExists(username)) {
            throw new IllegalArgumentException(
                    "That username is already in use."
            );
        }

        String email = optional(request.email(), 120);
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException(
                        "Enter a valid staff email address."
                );
            }
            if (staffDao.emailExists(email)) {
                throw new IllegalArgumentException(
                        "That email address is already in use."
                );
            }
        }

        validatePassword(request.password());
        User user = new User(
                0,
                username,
                PasswordUtil.hash(request.password()),
                require(request.fullName(), "Full name", 100),
                email,
                optional(request.contactNumber(), 20),
                request.role(),
                true,
                null
        );
        String registrationNumber = null;
        String specialization = null;
        BigDecimal consultationFee = null;

        if (request.role() == com.sunrisedental.model.Role.DENTIST) {
            registrationNumber = require(
                    request.registrationNumber(),
                    "Dentist registration number",
                    50
            ).toUpperCase(Locale.ROOT);
            specialization = optional(request.specialization(), 100);
            consultationFee = request.consultationFee();
            if (consultationFee == null || consultationFee.signum() < 0) {
                throw new IllegalArgumentException(
                        "Enter a valid dentist consultation fee."
                );
            }
            consultationFee = consultationFee.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return staffDao.create(
                user,
                registrationNumber,
                specialization,
                consultationFee
        );
    }

    public void setActive(
            long userId,
            boolean active,
            long currentUserId
    ) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Select a staff account.");
        }
        if (userId == currentUserId && !active) {
            throw new IllegalArgumentException(
                    "You cannot deactivate your own signed-in account."
            );
        }
        if (!staffDao.updateActive(userId, active)) {
            throw new IllegalArgumentException("Staff account was not found.");
        }
    }

    public void resetPassword(long userId, String password)
            throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Select a staff account.");
        }
        validatePassword(password);
        if (!staffDao.updatePassword(userId, PasswordUtil.hash(password))) {
            throw new IllegalArgumentException("Staff account was not found.");
        }
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < 10
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException(
                    "Password needs at least 10 characters with upper-case, lower-case, number and symbol."
            );
        }
    }

    private String require(String value, String name, int max) {
        String normalized = optional(value, max);
        if (normalized == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return normalized;
    }

    private String optional(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > max) {
            throw new IllegalArgumentException(
                    "A staff value exceeds the allowed length."
            );
        }
        return normalized;
    }
}

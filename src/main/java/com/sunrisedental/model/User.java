package com.sunrisedental.model;

import java.time.LocalDateTime;

public class User {

    private final long userId;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final String email;
    private final String contactNumber;
    private final Role role;
    private final boolean active;
    private final LocalDateTime createdAt;

    public User(
            long userId,
            String username,
            String passwordHash,
            String fullName,
            String email,
            String contactNumber,
            Role role,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
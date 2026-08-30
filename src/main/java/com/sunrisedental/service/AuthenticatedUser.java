package com.sunrisedental.service;

import com.sunrisedental.model.Role;

import java.io.Serializable;

public record AuthenticatedUser(
        long userId,
        String username,
        String fullName,
        Role role
) implements Serializable {
}
package com.sunrisedental.service;

import com.sunrisedental.model.Role;

public record AuthenticatedUser(
        long userId,
        String username,
        String fullName,
        Role role
) {
}
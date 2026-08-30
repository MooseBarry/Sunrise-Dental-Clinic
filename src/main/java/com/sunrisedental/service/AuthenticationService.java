package com.sunrisedental.service;

import com.sunrisedental.dao.UserDao;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthenticationService {

    private static final Logger LOGGER =
            Logger.getLogger(AuthenticationService.class.getName());

    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException(
                    "UserDao must not be null."
            );
        }

        this.userDao = userDao;
    }

    public AuthenticationResult authenticate(
            String username,
            String password
    ) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return AuthenticationResult.failure(
                    "Username and password are required."
            );
        }

        try {
            Optional<User> optionalUser =
                    userDao.findByUsername(username.trim());

            if (optionalUser.isEmpty()) {
                return invalidCredentials();
            }

            User user = optionalUser.get();

            if (!user.isActive()) {
                return invalidCredentials();
            }

            if (!PasswordUtil.matches(
                    password,
                    user.getPasswordHash()
            )) {
                return invalidCredentials();
            }

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            user.getUserId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getRole()
                    );

            return AuthenticationResult.success(authenticatedUser);

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Authentication database query failed.",
                    exception
            );

            return AuthenticationResult.failure(
                    "Unable to sign in at the moment."
            );
        }
    }

    private AuthenticationResult invalidCredentials() {
        return AuthenticationResult.failure(
                "Invalid username or password."
        );
    }
}
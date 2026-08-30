package com.sunrisedental.service;

import com.sunrisedental.dao.UserDao;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    private static final String CORRECT_PASSWORD =
            "SecurePass123!";

    private static String passwordHash;

    @BeforeAll
    static void createPasswordHash() {
        passwordHash = PasswordUtil.hash(CORRECT_PASSWORD);
    }

    @Test
    void shouldAuthenticateActiveUserWithCorrectPassword() {
        User user = createUser(true);
        AuthenticationService service =
                createServiceWith(user);

        AuthenticationResult result = service.authenticate(
                "admin",
                CORRECT_PASSWORD
        );

        assertTrue(result.successful());
        assertNotNull(result.user());
        assertEquals("admin", result.user().username());
        assertEquals(Role.ADMIN, result.user().role());
    }

    @Test
    void shouldRejectIncorrectPassword() {
        AuthenticationService service =
                createServiceWith(createUser(true));

        AuthenticationResult result = service.authenticate(
                "admin",
                "WrongPassword!"
        );

        assertFalse(result.successful());
        assertNull(result.user());
        assertEquals(
                "Invalid username or password.",
                result.message()
        );
    }

    @Test
    void shouldRejectUnknownUsername() {
        UserDao userDao = username -> Optional.empty();
        AuthenticationService service =
                new AuthenticationService(userDao);

        AuthenticationResult result = service.authenticate(
                "unknown",
                CORRECT_PASSWORD
        );

        assertFalse(result.successful());
        assertNull(result.user());
    }

    @Test
    void shouldRejectInactiveUser() {
        AuthenticationService service =
                createServiceWith(createUser(false));

        AuthenticationResult result = service.authenticate(
                "admin",
                CORRECT_PASSWORD
        );

        assertFalse(result.successful());
        assertNull(result.user());
    }

    @Test
    void shouldRejectBlankCredentials() {
        AuthenticationService service =
                createServiceWith(createUser(true));

        AuthenticationResult result =
                service.authenticate(" ", " ");

        assertFalse(result.successful());
        assertEquals(
                "Username and password are required.",
                result.message()
        );
    }

    private AuthenticationService createServiceWith(User user) {
        UserDao userDao = username -> {
            if ("admin".equalsIgnoreCase(username)) {
                return Optional.of(user);
            }

            return Optional.empty();
        };

        return new AuthenticationService(userDao);
    }

    private User createUser(boolean active) {
        return new User(
                1L,
                "admin",
                passwordHash,
                "System Administrator",
                "admin@sunrisedental.lk",
                "0770000000",
                Role.ADMIN,
                active,
                LocalDateTime.now()
        );
    }
}
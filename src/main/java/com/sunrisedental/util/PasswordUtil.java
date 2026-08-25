package com.sunrisedental.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        validatePassword(rawPassword);

        return BCrypt.hashpw(
                rawPassword,
                BCrypt.gensalt(LOG_ROUNDS)
        );
    }

    public static boolean matches(
            String rawPassword,
            String hashedPassword
    ) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }

        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password must not be blank."
            );
        }
    }
}
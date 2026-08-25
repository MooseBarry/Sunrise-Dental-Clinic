package com.sunrisedental.util;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        throw new UnsupportedOperationException(
                "Password hashing is not implemented yet."
        );
    }

    public static boolean matches(
            String rawPassword,
            String hashedPassword
    ) {
        return false;
    }
}
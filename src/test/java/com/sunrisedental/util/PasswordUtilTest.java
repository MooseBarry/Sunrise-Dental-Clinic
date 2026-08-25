package com.sunrisedental.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void shouldHashPasswordWithoutSavingPlainText() {
        String password = "SecurePass123!";

        String hash = PasswordUtil.hash(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
    }

    @Test
    void shouldMatchCorrectPassword() {
        String password = "SecurePass123!";
        String hash = PasswordUtil.hash(password);

        assertTrue(PasswordUtil.matches(password, hash));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String hash = PasswordUtil.hash("CorrectPass123!");

        assertFalse(
                PasswordUtil.matches("WrongPass123!", hash)
        );
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String password = "SecurePass123!";

        String firstHash = PasswordUtil.hash(password);
        String secondHash = PasswordUtil.hash(password);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void shouldRejectBlankPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash(" ")
        );
    }
}
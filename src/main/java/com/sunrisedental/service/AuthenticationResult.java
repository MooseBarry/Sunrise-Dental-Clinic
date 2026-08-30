package com.sunrisedental.service;

public record AuthenticationResult(
        boolean successful,
        AuthenticatedUser user,
        String message
) {

    public static AuthenticationResult success(
            AuthenticatedUser user
    ) {
        return new AuthenticationResult(
                true,
                user,
                "Authentication successful."
        );
    }

    public static AuthenticationResult failure(String message) {
        return new AuthenticationResult(
                false,
                null,
                message
        );
    }
}
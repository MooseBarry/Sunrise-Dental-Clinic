package com.sunrisedental.util;

public final class SessionConstants {

    public static final String AUTHENTICATED_USER =
            "authenticatedUser";

    public static final String CSRF_TOKEN =
            "csrfToken";

    public static final String LOGIN_FAILURES =
            "loginFailures";

    public static final String LOGIN_LOCK_UNTIL =
            "loginLockUntil";

    public static final int SESSION_TIMEOUT_SECONDS =
            30 * 60;

    private SessionConstants() {
    }
}

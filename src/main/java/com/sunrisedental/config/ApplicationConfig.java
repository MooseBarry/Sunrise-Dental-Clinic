package com.sunrisedental.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApplicationConfig {

    private static final Properties PROPERTIES = loadProperties();

    private ApplicationConfig() {
    }

    public static String required(
            String environmentName,
            String propertyName
    ) {
        String value = optional(
                environmentName,
                propertyName,
                null
        );

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing application setting: " + propertyName
            );
        }

        return value;
    }

    public static String optional(
            String environmentName,
            String propertyName,
            String defaultValue
    ) {
        String value = System.getenv(environmentName);

        if (value == null || value.isBlank()) {
            value = PROPERTIES.getProperty(propertyName);
        }

        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }

    public static boolean booleanValue(
            String environmentName,
            String propertyName,
            boolean defaultValue
    ) {
        return Boolean.parseBoolean(
                optional(
                        environmentName,
                        propertyName,
                        Boolean.toString(defaultValue)
                )
        );
    }

    public static int integerValue(
            String environmentName,
            String propertyName,
            int defaultValue
    ) {
        String value = optional(
                environmentName,
                propertyName,
                Integer.toString(defaultValue)
        );

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid numeric setting: " + propertyName,
                    exception
            );
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = ApplicationConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
            }

            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load application configuration.",
                    exception
            );
        }
    }
}

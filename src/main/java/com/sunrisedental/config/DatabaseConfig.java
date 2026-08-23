package com.sunrisedental.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                getValue("DB_URL", "database.url"),
                getValue("DB_USERNAME", "database.username"),
                getValue("DB_PASSWORD", "database.password")
        );
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
            }

            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load database configuration.",
                    exception
            );
        }
    }

    private static String getValue(
            String environmentName,
            String propertyName
    ) {
        String value = System.getenv(environmentName);

        if (value == null || value.isBlank()) {
            value = PROPERTIES.getProperty(propertyName);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing database setting: " + propertyName
            );
        }

        return value;
    }
}
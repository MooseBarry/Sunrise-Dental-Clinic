package com.sunrisedental.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "MySQL JDBC driver is not available.",
                    exception
            );
        }

        return DriverManager.getConnection(
                ApplicationConfig.required(
                        "DB_URL",
                        "database.url"
                ),
                ApplicationConfig.required(
                        "DB_USERNAME",
                        "database.username"
                ),
                ApplicationConfig.required(
                        "DB_PASSWORD",
                        "database.password"
                )
        );
    }
}

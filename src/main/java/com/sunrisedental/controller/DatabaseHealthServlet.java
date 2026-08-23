package com.sunrisedental.controller;

import com.sunrisedental.config.DatabaseConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/health/database")
public class DatabaseHealthServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement("SELECT 1");
                ResultSet resultSet = statement.executeQuery()
        ) {
            boolean healthy =
                    resultSet.next() && resultSet.getInt(1) == 1;

            if (healthy) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(
                        "{\"status\":\"UP\"," +
                                "\"database\":\"sunrise_dental\"}"
                );
            } else {
                writeUnavailableResponse(response);
            }
        } catch (SQLException | IllegalStateException exception) {
            log("Database health check failed.", exception);
            writeUnavailableResponse(response);
        }
    }

    private void writeUnavailableResponse(
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE
        );
        response.getWriter().write(
                "{\"status\":\"DOWN\"," +
                        "\"database\":\"sunrise_dental\"}"
        );
    }
}
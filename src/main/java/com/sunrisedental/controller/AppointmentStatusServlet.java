package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.service.AppointmentService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/appointments/status")
public class AppointmentStatusServlet
        extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    AppointmentStatusServlet.class.getName()
            );

    private AppointmentService appointmentService;

    @Override
    public void init() {
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao()
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String appointmentNumber =
                normalize(request.getParameter("number"));

        String statusValue =
                request.getParameter("status");

        String baseUrl = request.getContextPath()
                + "/appointments?number="
                + encode(appointmentNumber);

        try {
            AppointmentStatus status =
                    AppointmentStatus.valueOf(statusValue);

            appointmentService.changeStatus(
                    appointmentNumber,
                    status
            );

            response.sendRedirect(
                    baseUrl
                            + "&updated="
                            + encode(status.name())
            );

        } catch (
                IllegalArgumentException |
                NullPointerException exception
        ) {
            response.sendRedirect(
                    baseUrl
                            + "&actionError="
                            + encode(exception.getMessage())
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to update appointment status.",
                    exception
            );

            response.sendRedirect(
                    baseUrl
                            + "&actionError="
                            + encode(
                            "Unable to update appointment status."
                    )
            );
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}
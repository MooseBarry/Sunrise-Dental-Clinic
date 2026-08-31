package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.service.AppointmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/appointments")
public class AppointmentDirectoryServlet
        extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    AppointmentDirectoryServlet.class.getName()
            );

    private AppointmentService appointmentService;

    @Override
    public void init() {
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String appointmentNumber =
                request.getParameter("number");

        request.setAttribute(
                "createdAppointmentNumber",
                request.getParameter("created")
        );

        try {
            List<AppointmentDetails> appointments =
                    appointmentService.getAllAppointments();

            request.setAttribute(
                    "appointments",
                    appointments
            );

            if (appointmentNumber != null
                    && !appointmentNumber.isBlank()) {

                request.setAttribute(
                        "searchPerformed",
                        true
                );

                Optional<AppointmentDetails> result =
                        appointmentService
                                .findByAppointmentNumber(
                                        appointmentNumber
                                );

                if (result.isPresent()) {
                    request.setAttribute(
                            "appointment",
                            result.get()
                    );
                } else {
                    request.setAttribute(
                            "error",
                            "No appointment was found with "
                                    + "that appointment number."
                    );
                }
            }

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load appointments.",
                    exception
            );

            request.setAttribute(
                    "appointments",
                    List.of()
            );

            request.setAttribute(
                    "error",
                    "Unable to load appointment records."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/appointments/list.jsp"
        ).forward(request, response);
    }
}
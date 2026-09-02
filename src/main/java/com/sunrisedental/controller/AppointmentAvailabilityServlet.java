package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.service.AppointmentService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

@WebServlet("/api/appointments/availability")
public class AppointmentAvailabilityServlet extends HttpServlet {
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
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            boolean available = appointmentService.isDentistAvailable(
                    Long.parseLong(request.getParameter("dentistId")),
                    LocalDate.parse(request.getParameter("date")),
                    LocalTime.parse(request.getParameter("startTime")),
                    Integer.parseInt(request.getParameter("durationMinutes"))
            );
            response.getWriter().write(
                    "{\"available\":" + available + "}"
            );
        } catch (IllegalArgumentException | SQLException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"available\":false,\"error\":\"Invalid availability request.\"}"
            );
        }
    }
}

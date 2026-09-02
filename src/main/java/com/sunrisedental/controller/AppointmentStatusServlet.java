package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.dao.impl.JdbcNotificationDao;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.EmailService;
import com.sunrisedental.service.NotificationCoordinator;
import com.sunrisedental.service.NotificationService;
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
    private NotificationCoordinator notificationCoordinator;
    private AuditService auditService;

    @Override
    public void init() {
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao()
        );
        notificationCoordinator = new NotificationCoordinator(
                new NotificationService(new JdbcNotificationDao()),
                new EmailService()
        );
        auditService = new AuditService();
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

            AppointmentDetails updated = appointmentService.changeStatus(
                    appointmentNumber,
                    status
            );

            notificationCoordinator.appointmentStatusChanged(updated);
            AuthenticatedUser actor = (AuthenticatedUser)
                    request.getAttribute("currentUser");
            auditService.record(actor.userId(), "UPDATE_APPOINTMENT_STATUS",
                    "APPOINTMENT", appointmentNumber,
                    "Status: " + status.name());

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

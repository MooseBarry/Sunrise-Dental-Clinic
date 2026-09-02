package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.dao.impl.JdbcNotificationDao;
import com.sunrisedental.dao.impl.JdbcReportDao;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.NotificationService;
import com.sunrisedental.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(
            DashboardServlet.class.getName()
    );

    private AppointmentService appointmentService;
    private ReportService reportService;
    private NotificationService notificationService;

    @Override
    public void init() {
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao()
        );
        reportService = new ReportService(new JdbcReportDao());
        notificationService = new NotificationService(
                new JdbcNotificationDao()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        AuthenticatedUser user = (AuthenticatedUser)
                request.getAttribute("currentUser");
        LocalDate today = LocalDate.now();

        try {
            request.setAttribute(
                    "todayReport",
                    reportService.generate(today, today)
            );
            request.setAttribute(
                    "upcomingAppointments",
                    upcoming(appointmentService.getAllAppointments())
            );
            request.setAttribute(
                    "unreadCount",
                    notificationService.countUnread(user.userId())
            );
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING,
                    "Dashboard metrics could not be loaded.",
                    exception);
            request.setAttribute("dashboardWarning",
                    "Some live dashboard information is temporarily unavailable.");
            request.setAttribute("upcomingAppointments", List.of());
            request.setAttribute("unreadCount", 0);
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/dashboard.jsp"
        ).forward(request, response);
    }

    private List<AppointmentDetails> upcoming(
            List<AppointmentDetails> appointments
    ) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.stream()
                .filter(item -> item.status() == AppointmentStatus.SCHEDULED)
                .filter(item -> LocalDateTime.of(
                        item.appointmentDate(), item.startTime()
                ).isAfter(now))
                .sorted(Comparator.comparing(item -> LocalDateTime.of(
                        item.appointmentDate(), item.startTime()
                )))
                .limit(6)
                .toList();
    }
}

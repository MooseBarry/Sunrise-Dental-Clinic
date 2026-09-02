package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcNotificationDao;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    private NotificationService notificationService;

    @Override
    public void init() {
        notificationService = new NotificationService(
                new JdbcNotificationDao()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        AuthenticatedUser user = currentUser(request);
        try {
            request.setAttribute(
                    "notifications",
                    notificationService.getForUser(user.userId())
            );
            request.setAttribute(
                    "unreadCount",
                    notificationService.countUnread(user.userId())
            );
            request.getRequestDispatcher(
                    "/WEB-INF/views/notifications/list.jsp"
            ).forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException(
                    "Unable to load notifications.",
                    exception
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        AuthenticatedUser user = currentUser(request);
        try {
            if ("all".equals(request.getParameter("scope"))) {
                notificationService.markAllRead(user.userId());
            } else {
                notificationService.markRead(
                        parseId(request.getParameter("notificationId")),
                        user.userId()
                );
            }
            response.sendRedirect(
                    request.getContextPath() + "/notifications"
            );
        } catch (SQLException | IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Notification could not be updated."
            );
        }
    }

    private AuthenticatedUser currentUser(HttpServletRequest request) {
        return (AuthenticatedUser) request.getAttribute("currentUser");
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid notification.");
        }
    }
}

package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcStaffDao;
import com.sunrisedental.model.Role;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.StaffRegistrationRequest;
import com.sunrisedental.service.StaffService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/staff")
public class StaffManagementServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(
            StaffManagementServlet.class.getName()
    );

    private StaffService staffService;
    private AuditService auditService;

    @Override
    public void init() {
        staffService = new StaffService(new JdbcStaffDao());
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            request.setAttribute("staffAccounts", staffService.getAll());
            request.setAttribute("auditEntries", auditService.getRecent(30));
            request.setAttribute("roles", Role.values());
            exposeMessage(request);
            request.getRequestDispatcher(
                    "/WEB-INF/views/staff/list.jsp"
            ).forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException(
                    "Unable to load staff accounts.",
                    exception
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String action = value(request, "action");
        AuthenticatedUser actor =
                (AuthenticatedUser) request.getAttribute("currentUser");

        try {
            String message = switch (action) {
                case "create" -> create(request, actor);
                case "toggle" -> toggle(request, actor);
                case "reset-password" -> resetPassword(request, actor);
                default -> throw new IllegalArgumentException(
                        "Select a valid staff action."
                );
            };
            redirect(request, response, "success", message);
        } catch (IllegalArgumentException exception) {
            redirect(request, response, "error", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Staff update failed.", exception);
            redirect(request, response, "error",
                    "The staff account could not be updated.");
        }
    }

    private String create(
            HttpServletRequest request,
            AuthenticatedUser actor
    ) throws SQLException {
        Role role;
        try {
            role = Role.valueOf(value(request, "role")
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Select a valid role.");
        }

        long userId = staffService.register(
                new StaffRegistrationRequest(
                        value(request, "username"),
                        value(request, "password"),
                        value(request, "fullName"),
                        value(request, "email"),
                        value(request, "contactNumber"),
                        role,
                        value(request, "registrationNumber"),
                        value(request, "specialization"),
                        parseOptionalMoney(
                                value(request, "consultationFee")
                        )
                )
        );
        auditService.record(actor.userId(), "CREATE_STAFF", "USER",
                Long.toString(userId), "Role: " + role.name());
        return "Staff account created successfully.";
    }

    private String toggle(
            HttpServletRequest request,
            AuthenticatedUser actor
    ) throws SQLException {
        long userId = positiveLong(value(request, "userId"));
        boolean active = Boolean.parseBoolean(value(request, "active"));
        staffService.setActive(userId, active, actor.userId());
        auditService.record(actor.userId(), "SET_STAFF_ACTIVE", "USER",
                Long.toString(userId), "Active: " + active);
        return active
                ? "Staff account activated."
                : "Staff account deactivated.";
    }

    private String resetPassword(
            HttpServletRequest request,
            AuthenticatedUser actor
    ) throws SQLException {
        long userId = positiveLong(value(request, "userId"));
        staffService.resetPassword(userId, value(request, "newPassword"));
        auditService.record(actor.userId(), "RESET_PASSWORD", "USER",
                Long.toString(userId), null);
        return "Password reset successfully.";
    }

    private long positiveLong(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Select a staff account.");
        }
    }

    private BigDecimal parseOptionalMoney(String value) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Enter a valid consultation fee."
            );
        }
    }

    private void exposeMessage(HttpServletRequest request) {
        request.setAttribute("success", request.getParameter("success"));
        request.setAttribute("error", request.getParameter("error"));
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            String key,
            String message
    ) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff?" + key
                + "=" + URLEncoder.encode(
                message == null ? "" : message,
                StandardCharsets.UTF_8
        ));
    }
}

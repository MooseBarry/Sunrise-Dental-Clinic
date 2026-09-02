package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcTreatmentDao;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.TreatmentService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/treatments")
public class TreatmentManagementServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(
            TreatmentManagementServlet.class.getName()
    );

    private TreatmentService treatmentService;
    private AuditService auditService;

    @Override
    public void init() {
        treatmentService = new TreatmentService(new JdbcTreatmentDao());
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            request.setAttribute("treatments", treatmentService.getAll());
            request.setAttribute("success", request.getParameter("success"));
            request.setAttribute("error", request.getParameter("error"));
            request.getRequestDispatcher(
                    "/WEB-INF/views/treatments/list.jsp"
            ).forward(request, response);
        } catch (SQLException exception) {
            throw new ServletException(
                    "Unable to load treatments.",
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
            String message;
            if ("toggle".equals(action)) {
                long id = positiveLong(value(request, "treatmentId"));
                boolean active = Boolean.parseBoolean(
                        value(request, "active")
                );
                treatmentService.setActive(id, active);
                auditService.record(actor.userId(), "SET_TREATMENT_ACTIVE",
                        "TREATMENT", Long.toString(id),
                        "Active: " + active);
                message = active
                        ? "Treatment activated."
                        : "Treatment deactivated.";
            } else if ("save".equals(action)) {
                Long id = optionalLong(value(request, "treatmentId"));
                long savedId = treatmentService.save(
                        id,
                        value(request, "treatmentCode"),
                        value(request, "treatmentName"),
                        value(request, "description"),
                        parseMoney(value(request, "standardFee"))
                );
                auditService.record(actor.userId(),
                        id == null ? "CREATE_TREATMENT" : "UPDATE_TREATMENT",
                        "TREATMENT", Long.toString(savedId), null);
                message = id == null
                        ? "Treatment created successfully."
                        : "Treatment updated successfully.";
            } else {
                throw new IllegalArgumentException(
                        "Select a valid treatment action."
                );
            }
            redirect(request, response, "success", message);
        } catch (IllegalArgumentException exception) {
            redirect(request, response, "error", exception.getMessage());
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Treatment update failed.", exception);
            redirect(request, response, "error",
                    "Treatment could not be updated.");
        }
    }

    private BigDecimal parseMoney(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid standard fee.");
        }
    }

    private Long optionalLong(String value) {
        return value.isBlank() ? null : positiveLong(value);
    }

    private long positiveLong(String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Select a treatment.");
        }
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
        response.sendRedirect(request.getContextPath() + "/treatments?"
                + key + "=" + URLEncoder.encode(
                message == null ? "" : message,
                StandardCharsets.UTF_8
        ));
    }
}

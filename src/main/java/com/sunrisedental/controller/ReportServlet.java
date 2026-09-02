package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcReportDao;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    private ReportService reportService;
    private AuditService auditService;

    @Override
    public void init() {
        reportService = new ReportService(new JdbcReportDao());
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        LocalDate today = LocalDate.now();
        try {
            LocalDate from = parseDate(
                    request.getParameter("from"),
                    today.minusDays(29)
            );
            LocalDate to = parseDate(
                    request.getParameter("to"),
                    today
            );
            request.setAttribute("fromDate", from);
            request.setAttribute("toDate", to);
            request.setAttribute("report", reportService.generate(from, to));

            AuthenticatedUser actor = (AuthenticatedUser)
                    request.getAttribute("currentUser");
            auditService.record(actor.userId(), "GENERATE_REPORT",
                    "REPORT", from + "_" + to, null);
        } catch (IllegalArgumentException | SQLException exception) {
            request.setAttribute("error",
                    exception instanceof IllegalArgumentException
                            ? exception.getMessage()
                            : "The management report could not be generated.");
            request.setAttribute("fromDate", request.getParameter("from"));
            request.setAttribute("toDate", request.getParameter("to"));
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/reports/management.jsp"
        ).forward(request, response);
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Select valid report dates.");
        }
    }
}

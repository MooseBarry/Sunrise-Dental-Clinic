package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcBillDao;
import com.sunrisedental.dao.impl.JdbcNotificationDao;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.BillDetails;
import com.sunrisedental.model.PaymentMethod;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.BillingService;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.EmailService;
import com.sunrisedental.service.NotificationCoordinator;
import com.sunrisedental.service.NotificationService;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@WebServlet("/billing")
public class BillingDirectoryServlet extends HttpServlet {

    private BillingService billingService;
    private NotificationCoordinator notificationCoordinator;
    private AuditService auditService;

    @Override
    public void init() {
        billingService = new BillingService(
                new JdbcBillDao()
        );
        notificationCoordinator = new NotificationCoordinator(
                new NotificationService(new JdbcNotificationDao()),
                new EmailService()
        );
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            request.setAttribute(
                    "bills",
                    billingService.getAllBillDetails()
            );

            request.setAttribute(
                    "billableAppointments",
                    billingService
                            .getCompletedUnbilledAppointments()
            );

            request.setAttribute(
                    "paymentMethods",
                    PaymentMethod.values()
            );

            loadSelectedBill(request);
            loadMessages(request);

            request.getRequestDispatcher(
                    "/WEB-INF/views/billing/list.jsp"
            ).forward(request, response);
        } catch (RuntimeException exception) {
            throw new ServletException(
                    "Unable to load the billing directory.",
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

        String appointmentNumber =
                request.getParameter(
                        "appointmentNumber"
                );

        try {
            AuthenticatedUser currentUser =
                    requireCurrentUser(request);

            BigDecimal discount = parseMoney(
                    request.getParameter(
                            "discountAmount"
                    ),
                    true
            );

            Bill createdBill =
                    billingService.generateBill(
                            appointmentNumber,
                            discount,
                            currentUser.userId()
                    );

            billingService.findDetailsByBillNumber(
                    createdBill.billNumber()
            ).ifPresent(notificationCoordinator::billCreated);
            auditService.record(currentUser.userId(), "CREATE_INVOICE",
                    "BILL", createdBill.billNumber(),
                    "Appointment: " + appointmentNumber);

            redirect(
                    request,
                    response,
                    "?billNumber="
                            + encode(
                            createdBill.billNumber()
                    )
                            + "&created=true"
            );
        } catch (RuntimeException exception) {
            redirect(
                    request,
                    response,
                    "?appointmentNumber="
                            + encode(appointmentNumber)
                            + "&error="
                            + encode(exception.getMessage())
            );
        }
    }

    private void loadSelectedBill(
            HttpServletRequest request
    ) {
        String billNumber =
                request.getParameter("billNumber");

        if (billNumber == null || billNumber.isBlank()) {
            return;
        }

        Optional<BillDetails> selectedBill =
                billingService.findDetailsByBillNumber(
                        billNumber
                );

        if (selectedBill.isEmpty()) {
            request.setAttribute(
                    "error",
                    "The requested invoice was not found."
            );
            return;
        }

        BillDetails bill = selectedBill.get();

        request.setAttribute("selectedBill", bill);

        request.setAttribute(
                "payments",
                billingService.getPayments(
                        bill.billId()
                )
        );
    }

    private void loadMessages(
            HttpServletRequest request
    ) {
        String error = request.getParameter("error");

        if (error != null && !error.isBlank()) {
            request.setAttribute("error", error);
        }

        if ("true".equals(
                request.getParameter("created")
        )) {
            request.setAttribute(
                    "success",
                    "Invoice generated successfully."
            );
        }

        if ("true".equals(
                request.getParameter(
                        "paymentRecorded"
                )
        )) {
            request.setAttribute(
                    "success",
                    "Payment recorded successfully."
            );
        }
    }

    private AuthenticatedUser requireCurrentUser(
            HttpServletRequest request
    ) {
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            throw new IllegalStateException(
                    "Your session has expired. Please log in again."
            );
        }

        Object value = session.getAttribute(
                SessionConstants.AUTHENTICATED_USER
        );

        if (!(value instanceof AuthenticatedUser user)) {
            throw new IllegalStateException(
                    "Your session has expired. Please log in again."
            );
        }

        return user;
    }

    private BigDecimal parseMoney(
            String rawValue,
            boolean blankAsZero
    ) {
        if (rawValue == null || rawValue.isBlank()) {
            if (blankAsZero) {
                return BigDecimal.ZERO;
            }

            throw new IllegalArgumentException(
                    "Amount is required."
            );
        }

        try {
            return new BigDecimal(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Enter a valid monetary amount."
            );
        }
    }

    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            String query
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/billing"
                        + query
        );
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}

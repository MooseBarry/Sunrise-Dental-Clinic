package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcBillDao;
import com.sunrisedental.dao.impl.JdbcNotificationDao;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.PaymentMethod;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.BillingService;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.EmailService;
import com.sunrisedental.service.NotificationCoordinator;
import com.sunrisedental.service.NotificationService;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@WebServlet("/billing/payment")
public class BillingPaymentServlet extends HttpServlet {

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String billNumber =
                request.getParameter("billNumber");

        try {
            AuthenticatedUser currentUser =
                    requireCurrentUser(request);

            BigDecimal amount = parseAmount(
                    request.getParameter(
                            "paymentAmount"
                    )
            );

            PaymentMethod paymentMethod =
                    parsePaymentMethod(
                            request.getParameter(
                                    "paymentMethod"
                            )
                    );

            Bill updatedBill =
                    billingService.recordPayment(
                            billNumber,
                            amount,
                            paymentMethod,
                            currentUser.userId()
                    );

            billingService.findDetailsByBillNumber(
                    updatedBill.billNumber()
            ).ifPresent(details ->
                    notificationCoordinator.paymentRecorded(details, amount)
            );
            auditService.record(currentUser.userId(), "RECORD_PAYMENT",
                    "BILL", updatedBill.billNumber(),
                    "Amount: " + amount.toPlainString());

            redirect(
                    request,
                    response,
                    updatedBill.billNumber(),
                    "paymentRecorded=true"
            );
        } catch (RuntimeException exception) {
            redirect(
                    request,
                    response,
                    billNumber,
                    "error="
                            + encode(
                            exception.getMessage()
                    )
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

    private BigDecimal parseAmount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment amount is required."
            );
        }

        try {
            return new BigDecimal(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Enter a valid payment amount."
            );
        }
    }

    private PaymentMethod parsePaymentMethod(
            String rawValue
    ) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }

        try {
            return PaymentMethod.valueOf(
                    rawValue.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Select a valid payment method."
            );
        }
    }

    private void redirect(
            HttpServletRequest request,
            HttpServletResponse response,
            String billNumber,
            String result
    ) throws IOException {
        response.sendRedirect(
                request.getContextPath()
                        + "/billing?billNumber="
                        + encode(billNumber)
                        + "&"
                        + result
        );
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }
}

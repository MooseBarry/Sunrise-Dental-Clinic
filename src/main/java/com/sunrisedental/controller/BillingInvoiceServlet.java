package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcBillDao;
import com.sunrisedental.model.BillDetails;
import com.sunrisedental.service.BillingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/billing/invoice")
public class BillingInvoiceServlet extends HttpServlet {

    private BillingService billingService;

    @Override
    public void init() {
        billingService = new BillingService(
                new JdbcBillDao()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        String billNumber =
                request.getParameter("billNumber");

        if (billNumber == null || billNumber.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Bill number is required."
            );
            return;
        }

        try {
            BillDetails bill = billingService
                    .findDetailsByBillNumber(billNumber)
                    .orElse(null);

            if (bill == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Invoice was not found."
                );
                return;
            }

            request.setAttribute("bill", bill);
            request.setAttribute(
                    "payments",
                    billingService.getPayments(
                            bill.billId()
                    )
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/billing/invoice.jsp"
            ).forward(request, response);
        } catch (RuntimeException exception) {
            throw new ServletException(
                    "Unable to load the invoice.",
                    exception
            );
        }
    }
}
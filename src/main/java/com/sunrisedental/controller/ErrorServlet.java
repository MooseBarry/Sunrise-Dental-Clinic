package com.sunrisedental.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/error")
public class ErrorServlet extends HttpServlet {
    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        Object status = request.getAttribute(
                "jakarta.servlet.error.status_code"
        );
        int statusCode = status instanceof Integer value
                ? value
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        request.setAttribute("statusCode", statusCode);
        request.setAttribute(
                "errorTitle",
                switch (statusCode) {
                    case 403 -> "Access denied";
                    case 404 -> "Page not found";
                    default -> "Something went wrong";
                }
        );
        request.setAttribute(
                "errorMessage",
                switch (statusCode) {
                    case 403 -> "Your account does not have permission for this action.";
                    case 404 -> "The requested page could not be found.";
                    default -> "The system could not complete the request. Please try again.";
                }
        );
        response.setStatus(statusCode);
        request.getRequestDispatcher(
                "/WEB-INF/views/error.jsp"
        ).forward(request, response);
    }
}

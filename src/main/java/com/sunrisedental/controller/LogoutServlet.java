package com.sunrisedental.controller;

import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object value = session.getAttribute(
                    SessionConstants.AUTHENTICATED_USER
            );
            if (value instanceof AuthenticatedUser user) {
                new AuditService().record(
                        user.userId(),
                        "LOGOUT",
                        "USER",
                        user.username(),
                        null
                );
            }
            session.invalidate();
        }

        response.sendRedirect(
                request.getContextPath() + "/login?logout=true"
        );
    }
}

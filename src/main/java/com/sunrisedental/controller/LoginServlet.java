package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcUserDao;
import com.sunrisedental.service.AuthenticationResult;
import com.sunrisedental.service.AuthenticationService;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthenticationService authenticationService;
    private AuditService auditService;

    @Override
    public void init() {
        authenticationService =
                new AuthenticationService(new JdbcUserDao());
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null
                && session.getAttribute(
                SessionConstants.AUTHENTICATED_USER
        ) != null) {
            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );
            return;
        }

        if ("true".equals(request.getParameter("logout"))) {
            request.setAttribute(
                    "message",
                    "You have signed out successfully."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpSession session = request.getSession(true);
        Instant lockUntil = (Instant) session.getAttribute(
                SessionConstants.LOGIN_LOCK_UNTIL
        );
        if (lockUntil != null && lockUntil.isAfter(Instant.now())) {
            request.setAttribute("error",
                    "Too many unsuccessful attempts. Try again in a few minutes.");
            request.setAttribute("enteredUsername", username);
            request.getRequestDispatcher(
                    "/WEB-INF/views/login.jsp"
            ).forward(request, response);
            return;
        }

        AuthenticationResult result =
                authenticationService.authenticate(
                        username,
                        password
                );

        if (!result.successful()) {
            int failures = session.getAttribute(
                    SessionConstants.LOGIN_FAILURES
            ) instanceof Integer count ? count + 1 : 1;
            session.setAttribute(SessionConstants.LOGIN_FAILURES, failures);
            if (failures >= 5) {
                session.setAttribute(
                        SessionConstants.LOGIN_LOCK_UNTIL,
                        Instant.now().plusSeconds(300)
                );
                session.setAttribute(SessionConstants.LOGIN_FAILURES, 0);
            }
            request.setAttribute("error", result.message());
            request.setAttribute("enteredUsername", username);

            request.getRequestDispatcher(
                    "/WEB-INF/views/login.jsp"
            ).forward(request, response);
            return;
        }

        request.changeSessionId();

        session.removeAttribute(SessionConstants.LOGIN_FAILURES);
        session.removeAttribute(SessionConstants.LOGIN_LOCK_UNTIL);

        session.setMaxInactiveInterval(
                SessionConstants.SESSION_TIMEOUT_SECONDS
        );

        session.setAttribute(
                SessionConstants.AUTHENTICATED_USER,
                result.user()
        );

        auditService.record(
                result.user().userId(),
                "LOGIN_SUCCESS",
                "USER",
                result.user().username(),
                null
        );

        response.sendRedirect(
                request.getContextPath() + "/dashboard"
        );
    }
}

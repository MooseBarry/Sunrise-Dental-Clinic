package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcUserDao;
import com.sunrisedental.service.AuthenticationResult;
import com.sunrisedental.service.AuthenticationService;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private AuthenticationService authenticationService;

    @Override
    public void init() {
        authenticationService =
                new AuthenticationService(new JdbcUserDao());
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

        AuthenticationResult result =
                authenticationService.authenticate(
                        username,
                        password
                );

        if (!result.successful()) {
            request.setAttribute("error", result.message());
            request.setAttribute("enteredUsername", username);

            request.getRequestDispatcher(
                    "/WEB-INF/views/login.jsp"
            ).forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();

        session.setMaxInactiveInterval(
                SessionConstants.SESSION_TIMEOUT_SECONDS
        );

        session.setAttribute(
                SessionConstants.AUTHENTICATED_USER,
                result.user()
        );

        response.sendRedirect(
                request.getContextPath() + "/dashboard"
        );
    }
}
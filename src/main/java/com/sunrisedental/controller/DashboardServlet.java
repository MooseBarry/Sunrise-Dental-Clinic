package com.sunrisedental.controller;

import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        AuthenticatedUser user =
                (AuthenticatedUser) session.getAttribute(
                        SessionConstants.AUTHENTICATED_USER
                );

        request.setAttribute("currentUser", user);

        request.getRequestDispatcher(
                "/WEB-INF/views/dashboard.jsp"
        ).forward(request, response);
    }
}
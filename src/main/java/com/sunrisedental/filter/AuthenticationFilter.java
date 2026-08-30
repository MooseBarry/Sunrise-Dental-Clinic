package com.sunrisedental.filter;

import com.sunrisedental.util.SessionConstants;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {
        "/dashboard",
        "/patients",
        "/patients/*",
        "/appointments",
        "/appointments/*",
        "/billing",
        "/billing/*",
        "/reports",
        "/reports/*"
})
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession(false);

        boolean authenticated =
                session != null
                        && session.getAttribute(
                        SessionConstants.AUTHENTICATED_USER
                ) != null;

        if (!authenticated) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
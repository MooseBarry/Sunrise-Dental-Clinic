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
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter("/*")
public class CsrfFilter implements Filter {
    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

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

        if (isStaticAsset(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(
                SessionConstants.CSRF_TOKEN
        );
        if (token == null) {
            token = newToken();
            session.setAttribute(SessionConstants.CSRF_TOKEN, token);
        }
        request.setAttribute("csrfToken", token);

        if (requiresValidation(request)
                && !token.equals(request.getParameter("csrfToken"))) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "The form expired. Refresh the page and try again."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresValidation(HttpServletRequest request) {
        return switch (request.getMethod().toUpperCase()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    private boolean isStaticAsset(HttpServletRequest request) {
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());
        return path.startsWith("/assets/");
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }
}

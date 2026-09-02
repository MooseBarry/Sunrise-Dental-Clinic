package com.sunrisedental.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletResponse httpResponse =
                (HttpServletResponse) response;
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("Referrer-Policy",
                "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=()");
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self'; "
                        + "img-src 'self' data:; script-src 'self'; "
                        + "form-action 'self'; frame-ancestors 'none'");
        chain.doFilter(request, response);
    }
}

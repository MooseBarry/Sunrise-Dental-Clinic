package com.sunrisedental.filter;

import com.sunrisedental.model.Permission;
import com.sunrisedental.model.Role;
import com.sunrisedental.service.AuthenticatedUser;
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
        "/dashboard", "/patients", "/patients/*",
        "/appointments", "/appointments/*",
        "/billing", "/billing/*", "/treatments",
        "/treatments/*", "/staff", "/staff/*",
        "/reports", "/reports/*", "/notifications",
        "/notifications/*", "/help", "/logout",
        "/api/appointments/*"
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

        AuthenticatedUser user = currentUser(request);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        Permission required = requiredPermission(request);
        if (required != null && !user.role().allows(required)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this page."
            );
            return;
        }

        exposeNavigationPermissions(request, user.role());
        request.setAttribute("currentUser", user);
        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser currentUser(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(
                SessionConstants.AUTHENTICATED_USER
        );
        return value instanceof AuthenticatedUser user
                ? user
                : null;
    }

    private Permission requiredPermission(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());
        boolean post = "POST".equalsIgnoreCase(
                request.getMethod()
        );

        if (path.equals("/dashboard")) {
            return Permission.VIEW_DASHBOARD;
        }
        if (path.startsWith("/patients/new")
                || path.startsWith("/patients/edit")) {
            return Permission.MANAGE_PATIENTS;
        }
        if (path.startsWith("/patients")) {
            return post
                    ? Permission.MANAGE_PATIENTS
                    : Permission.VIEW_PATIENTS;
        }
        if (path.startsWith("/appointments/new")) {
            return Permission.MANAGE_APPOINTMENTS;
        }
        if (path.startsWith("/api/appointments")) {
            return Permission.VIEW_APPOINTMENTS;
        }
        if (path.startsWith("/appointments/status")) {
            return Permission.UPDATE_APPOINTMENT_STATUS;
        }
        if (path.startsWith("/appointments")) {
            return post
                    ? Permission.MANAGE_APPOINTMENTS
                    : Permission.VIEW_APPOINTMENTS;
        }
        if (path.startsWith("/billing")) {
            return post
                    ? Permission.MANAGE_BILLING
                    : Permission.VIEW_BILLING;
        }
        if (path.startsWith("/treatments")) {
            return Permission.MANAGE_TREATMENTS;
        }
        if (path.startsWith("/staff")) {
            return Permission.MANAGE_STAFF;
        }
        if (path.startsWith("/reports")) {
            return Permission.VIEW_REPORTS;
        }
        if (path.startsWith("/notifications")) {
            return Permission.VIEW_NOTIFICATIONS;
        }
        if (path.equals("/help")) {
            return Permission.VIEW_HELP;
        }
        return null;
    }

    private void exposeNavigationPermissions(
            HttpServletRequest request,
            Role role
    ) {
        request.setAttribute("canViewPatients",
                role.allows(Permission.VIEW_PATIENTS));
        request.setAttribute("canManagePatients",
                role.allows(Permission.MANAGE_PATIENTS));
        request.setAttribute("canViewAppointments",
                role.allows(Permission.VIEW_APPOINTMENTS));
        request.setAttribute("canManageAppointments",
                role.allows(Permission.MANAGE_APPOINTMENTS));
        request.setAttribute("canUpdateAppointments",
                role.allows(Permission.UPDATE_APPOINTMENT_STATUS));
        request.setAttribute("canViewBilling",
                role.allows(Permission.VIEW_BILLING));
        request.setAttribute("canManageBilling",
                role.allows(Permission.MANAGE_BILLING));
        request.setAttribute("canManageTreatments",
                role.allows(Permission.MANAGE_TREATMENTS));
        request.setAttribute("canManageStaff",
                role.allows(Permission.MANAGE_STAFF));
        request.setAttribute("canViewReports",
                role.allows(Permission.VIEW_REPORTS));
    }
}

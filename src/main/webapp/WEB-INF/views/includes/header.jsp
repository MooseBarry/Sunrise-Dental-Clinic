<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}"/> | Sunrise Dental</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260902-final">
    <script defer src="${pageContext.request.contextPath}/assets/js/app.js?v=20260902-final"></script>
</head>
<body>
<div class="app-layout">
    <aside class="sidebar">
        <a class="brand" href="${pageContext.request.contextPath}/dashboard">
            <span class="brand-mark">SD</span>
            <span><strong>Sunrise Dental</strong><small>Clinic Management</small></span>
        </a>

        <nav class="main-nav" aria-label="Main navigation">
            <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
            <c:if test="${canViewPatients}">
                <a href="${pageContext.request.contextPath}/patients">Patients</a>
            </c:if>
            <c:if test="${canViewAppointments}">
                <a href="${pageContext.request.contextPath}/appointments">Appointments</a>
            </c:if>
            <c:if test="${canViewBilling}">
                <a href="${pageContext.request.contextPath}/billing">Billing</a>
            </c:if>
            <c:if test="${canManageTreatments}">
                <a href="${pageContext.request.contextPath}/treatments">Treatments</a>
            </c:if>
            <c:if test="${canManageStaff}">
                <a href="${pageContext.request.contextPath}/staff">Staff</a>
            </c:if>
            <c:if test="${canViewReports}">
                <a href="${pageContext.request.contextPath}/reports">Reports</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/notifications">Notifications</a>
            <a href="${pageContext.request.contextPath}/help">Help</a>
        </nav>

        <div class="sidebar-user">
            <span class="avatar"><c:out value="${fn:substring(currentUser.fullName(), 0, 1)}"/></span>
            <span class="user-copy">
                <strong><c:out value="${currentUser.fullName()}"/></strong>
                <small><c:out value="${currentUser.role().displayName()}"/></small>
            </span>
            <form action="${pageContext.request.contextPath}/logout" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <button class="link-button" type="submit">Sign out</button>
            </form>
        </div>
    </aside>

    <main class="main-content">
        <header class="mobile-header">
            <span class="brand-mark">SD</span>
            <strong>Sunrise Dental Clinic</strong>
        </header>

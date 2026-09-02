<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Staff Sign In | Sunrise Dental</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260902-final">
</head>
<body class="login-page">
<main class="login-shell">
    <section class="login-visual">
        <span class="eyebrow">Sunrise Dental Clinic</span>
        <h1>Better care starts with organised information.</h1>
        <p>Securely manage patients, appointments, billing, notifications and clinic performance from one professional workspace.</p>
        <div class="login-feature-grid">
            <span>Secure staff access</span>
            <span>Conflict-free scheduling</span>
            <span>Accurate invoicing</span>
            <span>Management reporting</span>
        </div>
    </section>

    <section class="login-card">
        <div class="brand login-brand">
            <span class="brand-mark">SD</span>
            <span><strong>Sunrise Dental</strong><small>Clinic Management</small></span>
        </div>
        <div class="section-heading">
            <span class="eyebrow">Authorised staff only</span>
            <h2>Welcome back</h2>
            <p>Enter your staff credentials to continue.</p>
        </div>

        <c:if test="${not empty requestScope.error}">
            <div class="alert alert-error"><c:out value="${requestScope.error}"/></div>
        </c:if>
        <c:if test="${not empty requestScope.message}">
            <div class="alert alert-success"><c:out value="${requestScope.message}"/></div>
        </c:if>

        <form class="stack-form" action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <label for="username">Username</label>
            <input id="username" name="username" type="text" maxlength="50"
                   autocomplete="username" required autofocus
                   value="${fn:escapeXml(requestScope.enteredUsername)}">

            <label for="password">Password</label>
            <input id="password" name="password" type="password"
                   autocomplete="current-password" required>

            <button class="button button-primary button-block" type="submit">Sign in securely</button>
        </form>
        <p class="form-note">Your session automatically expires after 30 minutes of inactivity.</p>
    </section>
</main>
</body>
</html>

<%--
  Created by IntelliJ IDEA.
  User: Dulminnn
  Date: 8/30/2026
  Time: 4:59 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Staff Login | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="login-page">

<main class="login-card">
    <div class="brand-mark">SD</div>

    <p class="eyebrow">Sunrise Dental Clinic</p>
    <h1>Staff sign in</h1>

    <p class="subtitle">
        Enter your staff account details to continue.
    </p>

    <p class="success-message">${requestScope.message}</p>
    <p class="error-message">${requestScope.error}</p>

    <form method="post"
          action="${pageContext.request.contextPath}/login">

        <label for="username">Username</label>
        <input
                id="username"
                name="username"
                type="text"
                value="${requestScope.enteredUsername}"
                autocomplete="username"
                maxlength="50"
                required>

        <label for="password">Password</label>
        <input
                id="password"
                name="password"
                type="password"
                autocomplete="current-password"
                required>

        <button type="submit">Sign in</button>
    </form>
</main>

</body>
</html>
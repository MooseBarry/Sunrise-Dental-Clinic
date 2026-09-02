<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${errorTitle}"/> | Sunrise Dental</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260902-final">
</head>
<body class="error-page">
<main class="error-card">
    <span class="error-code"><c:out value="${statusCode}"/></span>
    <h1><c:out value="${errorTitle}"/></h1>
    <p><c:out value="${errorMessage}"/></p>
    <a class="button button-primary" href="${pageContext.request.contextPath}/dashboard">Return to dashboard</a>
</main>
</body>
</html>

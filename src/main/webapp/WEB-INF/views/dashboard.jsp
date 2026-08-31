<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Dashboard | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="dashboard-page">

<header class="topbar">
    <div>
        <p class="eyebrow">Sunrise Dental Clinic</p>
        <h1>Staff Dashboard</h1>
    </div>

    <form method="post"
          action="${pageContext.request.contextPath}/logout">
        <button class="logout-button" type="submit">
            Sign out
        </button>
    </form>
</header>

<main class="dashboard-content">
    <section class="welcome-panel">
        <p>Signed in as</p>
        <h2>${requestScope.currentUser.fullName()}</h2>
        <span class="role-badge">
            ${requestScope.currentUser.role()}
        </span>
    </section>

    <section class="feature-grid">

        <a class="feature-card feature-link"
           href="${pageContext.request.contextPath}/appointments">
            <span>01</span>
            <h3>Appointments</h3>
            <p>Register, search and manage clinic appointments.</p>
        </a>

        <a class="feature-card feature-link"
           href="${pageContext.request.contextPath}/patients">
            <span>02</span>
            <h3>Patients</h3>
            <p>Maintain patient and contact information.</p>
        </a>

        <article class="feature-card">
            <span>03</span>
            <h3>Billing</h3>
            <p>Calculate treatment charges and print bills.</p>
        </article>

        <article class="feature-card">
            <span>04</span>
            <h3>Reports</h3>
            <p>Review appointment and clinic activity reports.</p>
        </article>

    </section>
</main>

</body>
</html>

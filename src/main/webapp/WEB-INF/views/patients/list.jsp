<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Patients | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<header class="topbar">
    <div>
        <p class="eyebrow">Sunrise Dental Clinic</p>
        <h1>Patient Directory</h1>
    </div>

    <div class="topbar-actions">
        <a class="secondary-button"
           href="${pageContext.request.contextPath}/dashboard">
            Dashboard
        </a>

        <a class="primary-link"
           href="${pageContext.request.contextPath}/patients/new">
            Register patient
        </a>
    </div>
</header>

<main class="page-shell">

    <c:if test="${not empty createdPatientNumber}">
        <div class="notice success-notice">
            Patient registered successfully:
            <strong>
                <c:out value="${createdPatientNumber}"/>
            </strong>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="notice error-notice">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <section class="content-panel">
        <div class="section-heading">
            <div>
                <p class="eyebrow">Registered patients</p>
                <h2>Patient records</h2>
            </div>
        </div>

        <c:choose>
            <c:when test="${empty patients}">
                <div class="empty-state">
                    <h3>No patients registered</h3>
                    <p>
                        Register the first patient to begin
                        creating appointments.
                    </p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>Patient number</th>
                            <th>Name</th>
                            <th>Contact</th>
                            <th>NIC</th>
                            <th>Date of birth</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="patient"
                                   items="${patients}">
                            <tr>
                                <td>
                                    <strong>
                                        <c:out value="${patient.patientNumber()}"/>
                                    </strong>
                                </td>
                                <td>
                                    <c:out value="${patient.firstName()}"/>
                                    <c:out value="${patient.lastName()}"/>
                                </td>
                                <td>
                                    <c:out value="${patient.contactNumber()}"/>
                                </td>
                                <td>
                                    <c:out value="${patient.nicNumber()}"/>
                                </td>
                                <td>
                                    <c:out value="${patient.dateOfBirth()}"/>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

</body>
</html>
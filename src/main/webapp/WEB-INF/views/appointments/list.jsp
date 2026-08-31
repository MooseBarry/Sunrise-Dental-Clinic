<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Appointments | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<header class="topbar">
    <div>
        <p class="eyebrow">Sunrise Dental Clinic</p>
        <h1>Appointments</h1>
    </div>

    <div class="topbar-actions">
        <a class="secondary-button"
           href="${pageContext.request.contextPath}/dashboard">
            Dashboard
        </a>

        <a class="primary-link"
           href="${pageContext.request.contextPath}/appointments/new">
            New appointment
        </a>
    </div>
</header>

<main class="page-shell">

    <c:if test="${not empty createdAppointmentNumber}">
        <div class="notice success-notice">
            Appointment registered successfully:
            <strong>
                <c:out value="${createdAppointmentNumber}"/>
            </strong>
        </div>
    </c:if>

    <section class="content-panel search-panel">
        <p class="eyebrow">Appointment search</p>
        <h2>Find an appointment</h2>

        <form class="search-form"
              method="get"
              action="${pageContext.request.contextPath}/appointments">

            <input
                    name="number"
                    placeholder="Example: APT-20260905-B93AD0"
                    value="${fn:escapeXml(param.number)}"
                    maxlength="25"
                    required>

            <button type="submit">Search</button>

            <a class="cancel-link"
               href="${pageContext.request.contextPath}/appointments">
                Clear
            </a>
        </form>
    </section>

    <c:if test="${not empty error}">
        <div class="notice error-notice page-notice">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <c:if test="${not empty appointment}">
        <section class="content-panel details-panel">
            <div class="details-header">
                <div>
                    <p class="eyebrow">Appointment details</p>
                    <h2>
                        <c:out value="${appointment.appointmentNumber()}"/>
                    </h2>
                </div>

                <span class="status-badge">
                    <c:out value="${appointment.status()}"/>
                </span>
            </div>

            <div class="details-grid">
                <div>
                    <span>Patient</span>
                    <strong>
                        <c:out value="${appointment.patientName()}"/>
                    </strong>
                    <small>
                        <c:out value="${appointment.patientNumber()}"/>
                    </small>
                </div>

                <div>
                    <span>Contact</span>
                    <strong>
                        <c:out value="${appointment.patientContact()}"/>
                    </strong>
                    <small>
                        <c:out value="${appointment.patientAddress()}"/>
                    </small>
                </div>

                <div>
                    <span>Dentist</span>
                    <strong>
                        <c:out value="${appointment.dentistName()}"/>
                    </strong>
                    <small>
                        Registration:
                        <c:out value="${appointment.dentistRegistrationNumber()}"/>
                    </small>
                </div>

                <div>
                    <span>Treatment</span>
                    <strong>
                        <c:out value="${appointment.treatmentName()}"/>
                    </strong>
                    <small>
                        LKR
                        <c:out value="${appointment.treatmentTotal()}"/>
                    </small>
                </div>

                <div>
                    <span>Date and time</span>
                    <strong>
                        <c:out value="${appointment.appointmentDate()}"/>
                    </strong>
                    <small>
                        <c:out value="${appointment.startTime()}"/>
                        ·
                        <c:out value="${appointment.durationMinutes()}"/>
                        minutes
                    </small>
                </div>

                <div>
                    <span>Reason</span>
                    <strong>
                        <c:out value="${appointment.reason()}"/>
                    </strong>
                    <small>
                        <c:out value="${appointment.notes()}"/>
                    </small>
                </div>
            </div>
        </section>
    </c:if>

    <c:if test="${not searchPerformed}">
        <section class="content-panel appointment-list-panel">
            <p class="eyebrow">Appointment directory</p>
            <h2>All appointments</h2>

            <c:choose>
                <c:when test="${empty appointments}">
                    <div class="empty-state">
                        <h3>No appointments registered</h3>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="table-wrapper">
                        <table>
                            <thead>
                            <tr>
                                <th>Number</th>
                                <th>Patient</th>
                                <th>Dentist</th>
                                <th>Date</th>
                                <th>Time</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="item"
                                       items="${appointments}">
                                <tr>
                                    <td>
                                        <strong>
                                            <c:out value="${item.appointmentNumber()}"/>
                                        </strong>
                                    </td>
                                    <td>
                                        <c:out value="${item.patientName()}"/>
                                    </td>
                                    <td>
                                        <c:out value="${item.dentistName()}"/>
                                    </td>
                                    <td>
                                        <c:out value="${item.appointmentDate()}"/>
                                    </td>
                                    <td>
                                        <c:out value="${item.startTime()}"/>
                                    </td>
                                    <td>
                                        <span class="status-badge">
                                            <c:out value="${item.status()}"/>
                                        </span>
                                    </td>
                                    <td>
                                        <a class="table-link"
                                           href="${pageContext.request.contextPath}/appointments?number=${item.appointmentNumber()}">
                                            View
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </c:if>
</main>

</body>
</html>
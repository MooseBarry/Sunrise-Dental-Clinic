<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Register Appointment | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<header class="topbar">
    <div>
        <p class="eyebrow">Sunrise Dental Clinic</p>
        <h1>Register Appointment</h1>
    </div>

    <a class="secondary-button"
       href="${pageContext.request.contextPath}/dashboard">
        Dashboard
    </a>
</header>

<main class="page-shell">
    <section class="content-panel form-panel">

        <p class="eyebrow">Appointment information</p>
        <h2>Schedule a dental appointment</h2>

        <c:if test="${not empty createdAppointmentNumber}">
            <div class="notice success-notice">
                Appointment registered successfully:
                <strong>
                    <c:out value="${createdAppointmentNumber}"/>
                </strong>
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="notice error-notice">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/appointments/new">

            <div class="form-grid">

                <div class="full-width">
                    <label for="patientId">Patient *</label>

                    <select id="patientId"
                            name="patientId"
                            required>
                        <option value="">Select patient</option>

                        <c:forEach var="patient"
                                   items="${patients}">
                            <option
                                    value="${patient.patientId()}"
                                ${patientId == patient.patientId()
                                        ? 'selected' : ''}>
                                <c:out value="${patient.patientNumber()}"/>
                                -
                                <c:out value="${patient.firstName()}"/>
                                <c:out value="${patient.lastName()}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label for="dentistId">Dentist *</label>

                    <select id="dentistId"
                            name="dentistId"
                            required>
                        <option value="">Select dentist</option>

                        <c:forEach var="dentist"
                                   items="${dentists}">
                            <option
                                    value="${dentist.dentistId()}"
                                ${dentistId == dentist.dentistId()
                                        ? 'selected' : ''}>
                                <c:out value="${dentist.fullName()}"/>
                                -
                                <c:out value="${dentist.specialization()}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label for="treatmentId">Treatment *</label>

                    <select id="treatmentId"
                            name="treatmentId"
                            required>
                        <option value="">Select treatment</option>

                        <c:forEach var="treatment"
                                   items="${treatments}">
                            <option
                                    value="${treatment.treatmentId()}"
                                ${treatmentId == treatment.treatmentId()
                                        ? 'selected' : ''}>
                                <c:out value="${treatment.treatmentName()}"/>
                                - LKR
                                <c:out value="${treatment.standardFee()}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label for="appointmentDate">Date *</label>
                    <input id="appointmentDate"
                           name="appointmentDate"
                           type="date"
                           min="${today}"
                           value="${fn:escapeXml(appointmentDate)}"
                           required>
                </div>

                <div>
                    <label for="startTime">Start time *</label>
                    <input id="startTime"
                           name="startTime"
                           type="time"
                           value="${fn:escapeXml(startTime)}"
                           required>
                </div>

                <div>
                    <label for="durationMinutes">
                        Duration *
                    </label>

                    <select id="durationMinutes"
                            name="durationMinutes"
                            required>
                        <option value="30"
                        ${empty durationMinutes
                                || durationMinutes == '30'
                                ? 'selected' : ''}>
                            30 minutes
                        </option>

                        <option value="45"
                        ${durationMinutes == '45'
                                ? 'selected' : ''}>
                            45 minutes
                        </option>

                        <option value="60"
                        ${durationMinutes == '60'
                                ? 'selected' : ''}>
                            60 minutes
                        </option>

                        <option value="90"
                        ${durationMinutes == '90'
                                ? 'selected' : ''}>
                            90 minutes
                        </option>
                    </select>
                </div>

                <div>
                    <label for="reason">Reason</label>
                    <input id="reason"
                           name="reason"
                           maxlength="255"
                           value="${fn:escapeXml(reason)}"
                           placeholder="Example: Tooth pain">
                </div>

                <div class="full-width">
                    <label for="notes">Additional notes</label>
                    <textarea id="notes"
                              name="notes"
                              rows="4"><c:out value="${notes}"/></textarea>
                </div>
            </div>

            <div class="form-actions">
                <a class="cancel-link"
                   href="${pageContext.request.contextPath}/dashboard">
                    Cancel
                </a>

                <button class="submit-button"
                        type="submit"
                ${empty patients
                        || empty dentists
                        || empty treatments
                        ? 'disabled' : ''}>
                    Register appointment
                </button>
            </div>
        </form>
    </section>
</main>

</body>
</html>
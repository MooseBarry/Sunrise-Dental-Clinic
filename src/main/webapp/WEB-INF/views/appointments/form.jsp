<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Book Appointment" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div><span class="eyebrow">Scheduling</span><h1>Book an appointment</h1><p>The system checks the dentist's full appointment duration before reserving the time.</p></div>
    <a class="button button-secondary" href="${pageContext.request.contextPath}/appointments">Back to appointments</a>
</section>

<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<section class="panel form-panel">
    <form id="appointment-form" class="form-grid" method="post"
          data-availability-url="${pageContext.request.contextPath}/api/appointments/availability"
          action="${pageContext.request.contextPath}/appointments/new">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="field field-span-2">
            <label for="patientId">Patient <span>*</span></label>
            <select id="patientId" name="patientId" required>
                <option value="">Select a registered patient</option>
                <c:forEach var="patient" items="${patients}"><option value="${patient.patientId()}" ${patientId == patient.patientId() ? 'selected' : ''}><c:out value="${patient.patientNumber()} | ${patient.firstName()} ${patient.lastName()}"/></option></c:forEach>
            </select>
            <small>Patient not listed? <a href="${pageContext.request.contextPath}/patients/new">Register them first.</a></small>
        </div>
        <div class="field">
            <label for="dentistId">Dentist <span>*</span></label>
            <select id="dentistId" name="dentistId" required>
                <option value="">Select dentist</option>
                <c:forEach var="dentist" items="${dentists}"><option value="${dentist.dentistId()}" ${dentistId == dentist.dentistId() ? 'selected' : ''}><c:out value="${dentist.fullName()} | ${dentist.specialization()}"/></option></c:forEach>
            </select>
        </div>
        <div class="field">
            <label for="treatmentId">Treatment <span>*</span></label>
            <select id="treatmentId" name="treatmentId" required>
                <option value="">Select treatment</option>
                <c:forEach var="treatment" items="${treatments}"><option value="${treatment.treatmentId()}" ${treatmentId == treatment.treatmentId() ? 'selected' : ''}><c:out value="${treatment.treatmentName()} | LKR ${treatment.standardFee()}"/></option></c:forEach>
            </select>
        </div>
        <div class="field"><label for="appointmentDate">Date <span>*</span></label><input id="appointmentDate" name="appointmentDate" type="date" min="${today}" required value="${fn:escapeXml(appointmentDate)}"></div>
        <div class="field"><label for="startTime">Start time <span>*</span></label><input id="startTime" name="startTime" type="time" step="900" required value="${fn:escapeXml(startTime)}"></div>
        <div class="field">
            <label for="durationMinutes">Duration <span>*</span></label>
            <select id="durationMinutes" name="durationMinutes" required>
                <c:forEach var="minutes" items="${appointmentDurations}"><option value="${minutes}" ${durationMinutes == minutes || (empty durationMinutes && minutes == 30) ? 'selected' : ''}>${minutes} minutes</option></c:forEach>
            </select>
            <small>Every minute in this duration is protected from overlapping bookings.</small>
        </div>
        <div class="field"><label for="reason">Reason for visit</label><input id="reason" name="reason" maxlength="255" value="${fn:escapeXml(reason)}"></div>
        <div class="availability-check field-span-2"><button id="check-availability" class="button button-secondary" type="button">Check dentist availability</button><p id="availability-result" aria-live="polite">The final overlap check will also run when you confirm the booking.</p></div>
        <div class="field field-span-2"><label for="notes">Appointment notes</label><textarea id="notes" name="notes" maxlength="2000" rows="4"><c:out value="${notes}"/></textarea></div>
        <div class="form-actions field-span-2"><a class="button button-secondary" href="${pageContext.request.contextPath}/appointments">Cancel</a><button class="button button-primary" type="submit">Confirm booking</button></div>
    </form>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

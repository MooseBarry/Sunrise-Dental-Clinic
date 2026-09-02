<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Appointments" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div><span class="eyebrow">Clinic schedule</span><h1>Appointments</h1><p>Find appointments, review clinical details and update visit outcomes.</p></div>
    <c:if test="${canManageAppointments}"><a class="button button-primary" href="${pageContext.request.contextPath}/appointments/new">Book appointment</a></c:if>
</section>

<c:if test="${not empty createdAppointmentNumber}"><div class="alert alert-success">Appointment <strong><c:out value="${createdAppointmentNumber}"/></strong> booked successfully.</div></c:if>
<c:if test="${not empty updatedStatus}"><div class="alert alert-success">Appointment status updated to <strong><c:out value="${fn:replace(updatedStatus, '_', ' ')}"/></strong>.</div></c:if>
<c:if test="${not empty actionError}"><div class="alert alert-error"><c:out value="${actionError}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<section class="panel">
    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/appointments">
        <label class="sr-only" for="number">Appointment number</label>
        <input id="number" name="number" placeholder="Enter appointment number" value="${fn:escapeXml(param.number)}">
        <button class="button button-secondary" type="submit">Find appointment</button>
        <c:if test="${not empty param.number}"><a class="text-link" href="${pageContext.request.contextPath}/appointments">Clear</a></c:if>
    </form>
</section>

<c:if test="${not empty appointment}">
    <section class="panel detail-panel">
        <div class="panel-header"><div><span class="eyebrow">Search result</span><h2><c:out value="${appointment.appointmentNumber()}"/></h2></div><span class="status status-${fn:toLowerCase(fn:replace(appointment.status(), '_', '-'))}"><c:out value="${fn:replace(appointment.status(), '_', ' ')}"/></span></div>
        <div class="detail-grid">
            <div><span>Patient</span><strong><c:out value="${appointment.patientName()}"/></strong><small><c:out value="${appointment.patientNumber()}"/> · <c:out value="${appointment.patientContact()}"/></small></div>
            <div><span>Dentist</span><strong><c:out value="${appointment.dentistName()}"/></strong><small><c:out value="${appointment.dentistRegistrationNumber()}"/></small></div>
            <div><span>Date and time</span><strong><c:out value="${appointment.appointmentDate()}"/> at <c:out value="${appointment.startTime()}"/></strong><small><c:out value="${appointment.durationMinutes()}"/> minutes</small></div>
            <div><span>Treatment</span><strong><c:out value="${appointment.treatmentName()}"/></strong><small>LKR <c:out value="${appointment.treatmentTotal()}"/></small></div>
            <div class="field-span-2"><span>Reason / notes</span><strong><c:out value="${empty appointment.reason() ? 'No reason recorded' : appointment.reason()}"/></strong><small><c:out value="${empty appointment.notes() ? 'No additional notes' : appointment.notes()}"/></small></div>
        </div>
        <c:if test="${canChangeStatus && canUpdateAppointments}">
            <div class="status-actions"><span>Complete the visit outcome:</span>
                <c:forEach var="newStatus" items="${finalStatuses}">
                    <form method="post" action="${pageContext.request.contextPath}/appointments/status">
                        <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="number" value="${appointment.appointmentNumber()}"><input type="hidden" name="status" value="${newStatus}">
                        <button class="button button-small ${newStatus == 'COMPLETED' ? 'button-primary' : 'button-secondary'}" type="submit">${fn:replace(newStatus, '_', ' ')}</button>
                    </form>
                </c:forEach>
            </div>
        </c:if>
    </section>
</c:if>

<section class="panel">
    <div class="panel-header"><div><span class="eyebrow">Directory</span><h2>All appointments</h2></div><span class="record-count"><c:out value="${fn:length(appointments)}"/> records</span></div>
    <c:choose><c:when test="${empty appointments}"><div class="empty-state">No appointments have been registered.</div></c:when><c:otherwise>
        <div class="table-wrap"><table><thead><tr><th>Appointment</th><th>Patient</th><th>Dentist</th><th>Treatment</th><th>Date &amp; time</th><th>Status</th><th></th></tr></thead><tbody>
        <c:forEach var="item" items="${appointments}"><tr><td><strong><c:out value="${item.appointmentNumber()}"/></strong></td><td><c:out value="${item.patientName()}"/></td><td><c:out value="${item.dentistName()}"/></td><td><c:out value="${item.treatmentName()}"/></td><td><c:out value="${item.appointmentDate()}"/><small class="table-subtext"><c:out value="${item.startTime()}"/> · <c:out value="${item.durationMinutes()}"/> min</small></td><td><span class="status status-${fn:toLowerCase(fn:replace(item.status(), '_', '-'))}"><c:out value="${fn:replace(item.status(), '_', ' ')}"/></span></td><td><a class="text-link" href="${pageContext.request.contextPath}/appointments?number=${item.appointmentNumber()}">View</a></td></tr></c:forEach>
        </tbody></table></div>
    </c:otherwise></c:choose>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

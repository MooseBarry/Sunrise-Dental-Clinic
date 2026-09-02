<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Patients" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div><span class="eyebrow">Clinical directory</span><h1>Patients</h1><p>Search and maintain accurate patient information.</p></div>
    <c:if test="${canManagePatients}"><a class="button button-primary" href="${pageContext.request.contextPath}/patients/new">Register patient</a></c:if>
</section>

<c:if test="${not empty createdPatientNumber}"><div class="alert alert-success">Patient <strong><c:out value="${createdPatientNumber}"/></strong> registered successfully.</div></c:if>
<c:if test="${not empty updatedPatientNumber}"><div class="alert alert-success">Patient <strong><c:out value="${updatedPatientNumber}"/></strong> updated successfully.</div></c:if>
<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<section class="panel">
    <form class="search-bar" method="get" action="${pageContext.request.contextPath}/patients">
        <label class="sr-only" for="q">Search patients</label>
        <input id="q" name="q" maxlength="100" placeholder="Search number, name, NIC or contact" value="${fn:escapeXml(query)}">
        <button class="button button-secondary" type="submit">Search</button>
        <c:if test="${not empty query}"><a class="text-link" href="${pageContext.request.contextPath}/patients">Clear</a></c:if>
    </form>

    <c:choose>
        <c:when test="${empty patients}"><div class="empty-state">No patient records match this search.</div></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Patient</th><th>Contact</th><th>Date of birth</th><th>NIC</th><th class="align-right">Action</th></tr></thead>
                    <tbody>
                    <c:forEach var="patient" items="${patients}">
                        <tr>
                            <td><strong><c:out value="${patient.firstName()} ${patient.lastName()}"/></strong><small class="table-subtext"><c:out value="${patient.patientNumber()}"/></small></td>
                            <td><c:out value="${patient.contactNumber()}"/><c:if test="${not empty patient.email()}"><small class="table-subtext"><c:out value="${patient.email()}"/></small></c:if></td>
                            <td><c:out value="${empty patient.dateOfBirth() ? '—' : patient.dateOfBirth()}"/></td>
                            <td><c:out value="${empty patient.nicNumber() ? '—' : patient.nicNumber()}"/></td>
                            <td class="align-right"><c:if test="${canManagePatients}"><a class="button button-small button-secondary" href="${pageContext.request.contextPath}/patients/edit?id=${patient.patientId()}">Edit</a></c:if></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

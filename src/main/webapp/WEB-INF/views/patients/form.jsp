<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${editMode ? 'Edit Patient' : 'Register Patient'}" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div>
        <span class="eyebrow">Patient records</span>
        <h1>${editMode ? 'Update patient' : 'Register a new patient'}</h1>
        <p>${editMode ? 'Keep contact and clinical information accurate.' : 'Create a secure patient record before booking an appointment.'}</p>
    </div>
    <a class="button button-secondary" href="${pageContext.request.contextPath}/patients">Back to patients</a>
</section>

<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<section class="panel form-panel">
    <form class="form-grid" method="post"
          action="${pageContext.request.contextPath}${editMode ? '/patients/edit' : '/patients/new'}">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <c:if test="${editMode}">
            <input type="hidden" name="patientId" value="${patientId}">
            <div class="field field-span-2"><label>Patient number</label><input value="${fn:escapeXml(patientNumber)}" disabled></div>
        </c:if>

        <div class="field"><label for="firstName">First name <span>*</span></label><input id="firstName" name="firstName" maxlength="60" required value="${fn:escapeXml(firstName)}"></div>
        <div class="field"><label for="lastName">Last name <span>*</span></label><input id="lastName" name="lastName" maxlength="60" required value="${fn:escapeXml(lastName)}"></div>
        <div class="field"><label for="dateOfBirth">Date of birth</label><input id="dateOfBirth" name="dateOfBirth" type="date" max="${today}" value="${fn:escapeXml(dateOfBirth)}"></div>
        <div class="field"><label for="gender">Gender</label><select id="gender" name="gender"><option value="">Select</option><option value="Female" ${gender == 'Female' ? 'selected' : ''}>Female</option><option value="Male" ${gender == 'Male' ? 'selected' : ''}>Male</option><option value="Other" ${gender == 'Other' ? 'selected' : ''}>Other</option></select></div>
        <div class="field"><label for="nicNumber">NIC number</label><input id="nicNumber" name="nicNumber" maxlength="20" value="${fn:escapeXml(nicNumber)}"></div>
        <div class="field"><label for="contactNumber">Contact number <span>*</span></label><input id="contactNumber" name="contactNumber" maxlength="20" required value="${fn:escapeXml(contactNumber)}"></div>
        <div class="field field-span-2"><label for="email">Email address</label><input id="email" name="email" type="email" maxlength="120" value="${fn:escapeXml(email)}"><small>Used for appointment, invoice and payment notifications when email is enabled.</small></div>
        <div class="field field-span-2"><label for="address">Address</label><textarea id="address" name="address" maxlength="255" rows="3"><c:out value="${address}"/></textarea></div>
        <div class="field field-span-2"><label for="medicalNotes">Medical notes</label><textarea id="medicalNotes" name="medicalNotes" maxlength="2000" rows="4"><c:out value="${medicalNotes}"/></textarea><small>Record only information required for safe treatment.</small></div>

        <div class="form-actions field-span-2">
            <a class="button button-secondary" href="${pageContext.request.contextPath}/patients">Cancel</a>
            <button class="button button-primary" type="submit">${editMode ? 'Save changes' : 'Register patient'}</button>
        </div>
    </form>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

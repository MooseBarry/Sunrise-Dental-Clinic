<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Staff Management" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading"><div><span class="eyebrow">Administration</span><h1>Staff management</h1><p>Create secure accounts, assign roles and control access.</p></div></section>
<c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<section class="panel form-panel">
    <div class="panel-header"><div><span class="eyebrow">New account</span><h2>Add staff member</h2></div></div>
    <form class="form-grid" method="post" action="${pageContext.request.contextPath}/staff">
        <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="create">
        <div class="field"><label for="fullName">Full name <span>*</span></label><input id="fullName" name="fullName" maxlength="100" required></div>
        <div class="field"><label for="username">Username <span>*</span></label><input id="username" name="username" maxlength="50" autocomplete="off" required></div>
        <div class="field"><label for="email">Email</label><input id="email" name="email" type="email" maxlength="120"></div>
        <div class="field"><label for="contactNumber">Contact number</label><input id="contactNumber" name="contactNumber" maxlength="20"></div>
        <div class="field"><label for="role">Role <span>*</span></label><select id="role" name="role" required><c:forEach var="role" items="${roles}"><option value="${role.name()}"><c:out value="${role.displayName()}"/></option></c:forEach></select></div>
        <div class="field"><label for="password">Temporary password <span>*</span></label><input id="password" name="password" type="password" autocomplete="new-password" required><small>At least 10 characters with upper/lower-case, number and symbol.</small></div>
        <div class="field"><label for="registrationNumber">Dentist registration number</label><input id="registrationNumber" name="registrationNumber" maxlength="50"><small>Required only when the selected role is Dentist.</small></div>
        <div class="field"><label for="specialization">Dentist specialization</label><input id="specialization" name="specialization" maxlength="100"></div>
        <div class="field"><label for="consultationFee">Dentist consultation fee (LKR)</label><input id="consultationFee" name="consultationFee" type="number" min="0" step="0.01"></div>
        <div class="form-actions field-span-2"><button class="button button-primary" type="submit">Create staff account</button></div>
    </form>
</section>

<section class="panel">
    <div class="panel-header"><div><span class="eyebrow">Access directory</span><h2>Staff accounts</h2></div><span class="record-count"><c:out value="${fn:length(staffAccounts)}"/> accounts</span></div>
    <div class="table-wrap"><table><thead><tr><th>Staff member</th><th>Role</th><th>Contact</th><th>Status</th><th>Security actions</th></tr></thead><tbody>
    <c:forEach var="staff" items="${staffAccounts}"><tr><td><strong><c:out value="${staff.fullName()}"/></strong><small class="table-subtext">@<c:out value="${staff.username()}"/> · Added <c:out value="${staff.createdAtDisplay()}"/></small></td><td><span class="role-pill"><c:out value="${staff.role().displayName()}"/></span></td><td><c:out value="${empty staff.email() ? '—' : staff.email()}"/><small class="table-subtext"><c:out value="${empty staff.contactNumber() ? '' : staff.contactNumber()}"/></small></td><td><span class="status ${staff.active() ? 'status-active' : 'status-cancelled'}">${staff.active() ? 'ACTIVE' : 'INACTIVE'}</span></td><td><div class="table-actions"><form method="post" action="${pageContext.request.contextPath}/staff"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="toggle"><input type="hidden" name="userId" value="${staff.userId()}"><input type="hidden" name="active" value="${!staff.active()}"><button class="button button-small button-secondary" type="submit">${staff.active() ? 'Deactivate' : 'Activate'}</button></form><form class="reset-form" method="post" action="${pageContext.request.contextPath}/staff"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="reset-password"><input type="hidden" name="userId" value="${staff.userId()}"><input aria-label="New password for ${staff.username()}" name="newPassword" type="password" placeholder="New password" required><button class="button button-small button-secondary" type="submit">Reset</button></form></div></td></tr></c:forEach>
    </tbody></table></div>
</section>
<section class="panel"><div class="panel-header"><div><span class="eyebrow">Security oversight</span><h2>Recent audit activity</h2></div></div><c:choose><c:when test="${empty auditEntries}"><div class="empty-state">No audit activity has been recorded yet.</div></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Date</th><th>Staff member</th><th>Action</th><th>Entity</th><th>Details</th></tr></thead><tbody><c:forEach var="entry" items="${auditEntries}"><tr><td><c:out value="${entry.createdAtDisplay()}"/></td><td><c:out value="${entry.actorName()}"/></td><td><c:out value="${fn:replace(entry.actionName(), '_', ' ')}"/></td><td><c:out value="${entry.entityType()}"/> <c:out value="${entry.entityReference()}"/></td><td><c:out value="${empty entry.details() ? '—' : entry.details()}"/></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose></section>
<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

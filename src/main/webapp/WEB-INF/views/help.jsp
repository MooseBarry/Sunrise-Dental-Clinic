<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Help Centre" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div><span class="eyebrow">Guidance</span><h1>Help centre</h1><p>Quick instructions for the clinic's main workflows.</p></div>
</section>

<section class="help-grid">
    <article class="panel"><h2>Patients</h2><p>Receptionists and administrators can register patients, search the directory and update contact or medical information.</p></article>
    <article class="panel"><h2>Appointments</h2><p>Select a patient, dentist, treatment, date, start time and duration. The system blocks every overlapping period—not only identical start times.</p></article>
    <article class="panel"><h2>Billing</h2><p>Cashiers and administrators can select a completed, unbilled appointment, generate an invoice, record partial or full payments, and print the invoice or receipt history.</p></article>
    <article class="panel"><h2>Notifications</h2><p>Operational events appear in the notification centre. Patient emails are also sent when SMTP settings are enabled and the patient has an email address.</p></article>
    <article class="panel"><h2>Reports</h2><p>Administrators can select a date range of up to one year to review appointments, revenue, outstanding balances, treatments and dentist workload.</p></article>
    <article class="panel"><h2>Security</h2><p>Never share accounts. Sign out when leaving a workstation. Administrators should deactivate unused accounts and issue unique temporary passwords.</p></article>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

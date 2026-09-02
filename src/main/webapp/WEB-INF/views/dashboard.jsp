<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div>
        <span class="eyebrow">Clinic overview</span>
        <h1>Good day, <c:out value="${currentUser.fullName()}"/></h1>
        <p>Here is the latest operational picture for Sunrise Dental Clinic.</p>
    </div>
    <span class="role-pill"><c:out value="${currentUser.role().displayName()}"/></span>
</section>

<c:if test="${not empty dashboardWarning}">
    <div class="alert alert-warning"><c:out value="${dashboardWarning}"/></div>
</c:if>

<section class="metric-grid">
    <c:if test="${canViewAppointments}">
    <article class="metric-card">
        <span>Today's appointments</span>
        <strong><c:out value="${empty todayReport ? 0 : todayReport.totalAppointments()}"/></strong>
        <small><c:out value="${empty todayReport ? 0 : todayReport.scheduledAppointments()}"/> still scheduled</small>
    </article>
    <article class="metric-card">
        <span>Completed today</span>
        <strong><c:out value="${empty todayReport ? 0 : todayReport.completedAppointments()}"/></strong>
        <small>Clinical visits completed</small>
    </article>
    </c:if>
    <c:if test="${canViewBilling}">
    <article class="metric-card">
        <span>Revenue received</span>
        <strong>LKR <c:out value="${empty todayReport ? '0.00' : todayReport.receivedAmount()}"/></strong>
        <small>Payments recorded today</small>
    </article>
    </c:if>
    <article class="metric-card">
        <span>Unread notifications</span>
        <strong><c:out value="${unreadCount}"/></strong>
        <small><a href="${pageContext.request.contextPath}/notifications">Open notification centre</a></small>
    </article>
</section>

<section class="content-grid content-grid-main">
    <c:if test="${canViewAppointments}">
    <article class="panel">
        <div class="panel-header">
            <div><span class="eyebrow">Schedule</span><h2>Upcoming appointments</h2></div>
            <c:if test="${canViewAppointments}">
                <a class="text-link" href="${pageContext.request.contextPath}/appointments">View directory</a>
            </c:if>
        </div>
        <c:choose>
            <c:when test="${empty upcomingAppointments}">
                <div class="empty-state">No upcoming scheduled appointments were found.</div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead><tr><th>Appointment</th><th>Patient</th><th>Dentist</th><th>Date &amp; time</th></tr></thead>
                        <tbody>
                        <c:forEach var="item" items="${upcomingAppointments}">
                            <tr>
                                <td><strong><c:out value="${item.appointmentNumber()}"/></strong></td>
                                <td><c:out value="${item.patientName()}"/></td>
                                <td><c:out value="${item.dentistName()}"/></td>
                                <td><c:out value="${item.appointmentDate()}"/> · <c:out value="${item.startTime()}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </article>
    </c:if>

    <aside class="panel quick-actions">
        <div class="panel-header"><div><span class="eyebrow">Shortcuts</span><h2>Quick actions</h2></div></div>
        <c:if test="${canManagePatients}"><a href="${pageContext.request.contextPath}/patients/new"><strong>Register patient</strong><span>Create a new clinical record</span></a></c:if>
        <c:if test="${canManageAppointments}"><a href="${pageContext.request.contextPath}/appointments/new"><strong>Book appointment</strong><span>Check availability and reserve time</span></a></c:if>
        <c:if test="${canManageBilling}"><a href="${pageContext.request.contextPath}/billing"><strong>Create invoice</strong><span>Bill a completed appointment</span></a></c:if>
        <c:if test="${canViewReports}"><a href="${pageContext.request.contextPath}/reports"><strong>Management report</strong><span>Review performance and revenue</span></a></c:if>
    </aside>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

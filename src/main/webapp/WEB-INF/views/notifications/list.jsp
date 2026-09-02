<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Notifications" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading"><div><span class="eyebrow">Activity centre</span><h1>Notifications</h1><p>Important appointment, billing, payment and system events.</p></div><c:if test="${unreadCount gt 0}"><form method="post" action="${pageContext.request.contextPath}/notifications"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="scope" value="all"><button class="button button-secondary" type="submit">Mark all as read</button></form></c:if></section>

<section class="panel notification-list"><c:choose><c:when test="${empty notifications}"><div class="empty-state">You do not have any notifications yet.</div></c:when><c:otherwise><c:forEach var="notice" items="${notifications}"><article class="notification-item ${notice.unread() ? 'notification-unread' : ''}"><span class="notification-type"><c:out value="${notice.notificationType().displayName()}"/></span><div><div class="notification-heading"><h2><c:out value="${notice.title()}"/></h2><time><c:out value="${notice.createdAtDisplay()}"/></time></div><p><c:out value="${notice.message()}"/></p><c:if test="${not empty notice.referenceValue()}"><small>Reference: <c:out value="${notice.referenceValue()}"/></small></c:if></div><c:if test="${notice.unread()}"><form method="post" action="${pageContext.request.contextPath}/notifications"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="notificationId" value="${notice.notificationId()}"><button class="button button-small button-secondary" type="submit">Mark read</button></form></c:if></article></c:forEach></c:otherwise></c:choose></section>
<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

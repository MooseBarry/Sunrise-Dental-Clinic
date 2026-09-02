<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Billing" scope="request"/>
<jsp:include page="/WEB-INF/views/includes/header.jsp"/>

<section class="page-heading">
    <div><span class="eyebrow">Financial operations</span><h1>Billing and payments</h1><p>Generate accurate invoices from completed appointments and maintain a complete payment trail.</p></div>
</section>

<c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
<c:if test="${not empty error}"><div class="alert alert-error"><c:out value="${error}"/></div></c:if>

<c:if test="${canManageBilling}">
<section class="panel">
    <div class="panel-header"><div><span class="eyebrow">New invoice</span><h2>Select a completed appointment</h2></div></div>
    <c:choose>
        <c:when test="${empty billableAppointments}"><div class="empty-state">There are no completed, unbilled appointments. Complete an appointment first or review the invoice directory below.</div></c:when>
        <c:otherwise>
            <form class="form-grid compact-form" method="post" action="${pageContext.request.contextPath}/billing">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <div class="field field-span-2"><label for="appointmentNumber">Completed appointment <span>*</span></label><select id="appointmentNumber" name="appointmentNumber" required><option value="">Choose an appointment</option><c:forEach var="appointment" items="${billableAppointments}"><option value="${appointment.appointmentNumber()}" ${param.appointmentNumber == appointment.appointmentNumber() ? 'selected' : ''}><c:out value="${appointment.optionLabel()}"/></option></c:forEach></select><small>Only completed appointments without an existing invoice are shown.</small></div>
                <div class="field"><label for="discountAmount">Discount (LKR)</label><input id="discountAmount" name="discountAmount" type="number" min="0" step="0.01" value="0.00"></div>
                <div class="form-actions"><button class="button button-primary" type="submit">Generate invoice</button></div>
            </form>
        </c:otherwise>
    </c:choose>
</section>
</c:if>

<c:if test="${not empty selectedBill}">
<section class="panel invoice-summary">
    <div class="panel-header"><div><span class="eyebrow">Selected invoice</span><h2><c:out value="${selectedBill.billNumber()}"/></h2></div><span class="status status-${fn:toLowerCase(fn:replace(selectedBill.paymentStatus().name(), '_', '-'))}"><c:out value="${fn:replace(selectedBill.paymentStatus().name(), '_', ' ')}"/></span></div>
    <div class="detail-grid detail-grid-money">
        <div><span>Patient</span><strong><c:out value="${selectedBill.patientName()}"/></strong><small><c:out value="${selectedBill.patientNumber()}"/></small></div>
        <div><span>Appointment</span><strong><c:out value="${selectedBill.appointmentNumber()}"/></strong><small><c:out value="${selectedBill.appointmentDateDisplay()}"/> at <c:out value="${selectedBill.appointmentTimeDisplay()}"/></small></div>
        <div><span>Invoice total</span><strong>LKR <c:out value="${selectedBill.totalAmount()}"/></strong><small>Discount: LKR <c:out value="${selectedBill.discountAmount()}"/></small></div>
        <div><span>Outstanding</span><strong>LKR <c:out value="${selectedBill.outstandingAmount()}"/></strong><small>Paid: LKR <c:out value="${selectedBill.amountPaid()}"/></small></div>
    </div>
    <div class="invoice-actions"><a class="button button-secondary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/billing/invoice?billNumber=${selectedBill.billNumber()}">Open printable invoice</a></div>

    <c:if test="${canManageBilling && selectedBill.paymentStatus().name() ne 'PAID'}">
        <div class="subpanel"><h3>Record a payment</h3><form class="inline-form" method="post" action="${pageContext.request.contextPath}/billing/payment"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="billNumber" value="${selectedBill.billNumber()}"><div class="field"><label for="paymentAmount">Amount (LKR)</label><input id="paymentAmount" name="paymentAmount" type="number" min="0.01" max="${selectedBill.outstandingAmount()}" step="0.01" required></div><div class="field"><label for="paymentMethod">Payment method</label><select id="paymentMethod" name="paymentMethod" required><c:forEach var="method" items="${paymentMethods}"><option value="${method.name()}"><c:out value="${method.getDisplayName()}"/></option></c:forEach></select></div><button class="button button-primary" type="submit">Record payment</button></form></div>
    </c:if>

    <div class="subpanel"><h3>Payment history</h3><c:choose><c:when test="${empty payments}"><p class="muted">No payments have been recorded.</p></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Receipt</th><th>Date</th><th>Method</th><th class="align-right">Amount</th></tr></thead><tbody><c:forEach var="payment" items="${payments}"><tr><td><strong><c:out value="${payment.receiptNumber()}"/></strong></td><td><c:out value="${payment.paidAtDisplay()}"/></td><td><c:out value="${payment.paymentMethod().getDisplayName()}"/></td><td class="align-right">LKR <c:out value="${payment.amount()}"/></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose></div>
</section>
</c:if>

<section class="panel">
    <div class="panel-header"><div><span class="eyebrow">Directory</span><h2>Invoices</h2></div><span class="record-count"><c:out value="${fn:length(bills)}"/> records</span></div>
    <c:choose><c:when test="${empty bills}"><div class="empty-state">No invoices have been generated.</div></c:when><c:otherwise><div class="table-wrap"><table><thead><tr><th>Invoice</th><th>Patient</th><th>Appointment</th><th>Total</th><th>Paid</th><th>Balance</th><th>Status</th><th></th></tr></thead><tbody><c:forEach var="invoice" items="${bills}"><tr><td><strong><c:out value="${invoice.billNumber()}"/></strong></td><td><c:out value="${invoice.patientName()}"/></td><td><c:out value="${invoice.appointmentNumber()}"/></td><td>LKR <c:out value="${invoice.totalAmount()}"/></td><td>LKR <c:out value="${invoice.amountPaid()}"/></td><td>LKR <c:out value="${invoice.outstandingAmount()}"/></td><td><span class="status status-${fn:toLowerCase(fn:replace(invoice.paymentStatus().name(), '_', '-'))}"><c:out value="${fn:replace(invoice.paymentStatus().name(), '_', ' ')}"/></span></td><td><a class="text-link" href="${pageContext.request.contextPath}/billing?billNumber=${invoice.billNumber()}">View</a></td></tr></c:forEach></tbody></table></div></c:otherwise></c:choose>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

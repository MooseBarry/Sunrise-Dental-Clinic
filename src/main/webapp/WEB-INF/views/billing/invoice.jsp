<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice <c:out value="${bill.billNumber()}"/> | Sunrise Dental</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20260902-final">
    <script defer src="${pageContext.request.contextPath}/assets/js/app.js?v=20260902-final"></script>
</head>
<body class="print-page">
<div class="print-toolbar"><a class="button button-secondary" href="${pageContext.request.contextPath}/billing?billNumber=${bill.billNumber()}">Back to billing</a><button class="button button-primary" type="button" data-print>Print invoice</button></div>
<main class="invoice-document">
    <header class="invoice-header"><div class="brand"><span class="brand-mark">SD</span><span><strong>Sunrise Dental Clinic</strong><small>Colombo, Sri Lanka</small></span></div><div class="invoice-title"><span>Invoice</span><h1><c:out value="${bill.billNumber()}"/></h1><small>Issued <c:out value="${bill.issuedAtDisplay()}"/></small></div></header>
    <section class="invoice-parties"><div><span class="eyebrow">Bill to</span><h2><c:out value="${bill.patientName()}"/></h2><p><c:out value="${bill.patientNumber()}"/><br><c:out value="${bill.patientContact()}"/><c:if test="${not empty bill.patientEmail()}"><br><c:out value="${bill.patientEmail()}"/></c:if><c:if test="${not empty bill.patientAddress()}"><br><c:out value="${bill.patientAddress()}"/></c:if></p></div><div><span class="eyebrow">Appointment</span><h2><c:out value="${bill.appointmentNumber()}"/></h2><p><c:out value="${bill.appointmentDateDisplay()}"/> at <c:out value="${bill.appointmentTimeDisplay()}"/><br><c:out value="${bill.dentistName()}"/></p></div></section>
    <table class="invoice-table"><thead><tr><th>Description</th><th class="align-right">Amount (LKR)</th></tr></thead><tbody><tr><td>Consultation fee</td><td class="align-right"><c:out value="${bill.consultationFee()}"/></td></tr><tr><td><strong>Treatment</strong><small class="table-subtext"><c:out value="${bill.treatmentSummary()}"/></small></td><td class="align-right"><c:out value="${bill.treatmentTotal()}"/></td></tr><c:if test="${bill.discountAmount().signum() gt 0}"><tr><td>Discount</td><td class="align-right">− <c:out value="${bill.discountAmount()}"/></td></tr></c:if></tbody></table>
    <section class="invoice-totals"><div><span>Payment status</span><strong class="status status-${fn:toLowerCase(fn:replace(bill.paymentStatus().name(), '_', '-'))}"><c:out value="${fn:replace(bill.paymentStatus().name(), '_', ' ')}"/></strong></div><dl><dt>Total</dt><dd>LKR <c:out value="${bill.totalAmount()}"/></dd><dt>Paid</dt><dd>LKR <c:out value="${bill.amountPaid()}"/></dd><dt class="grand-total">Balance due</dt><dd class="grand-total">LKR <c:out value="${bill.outstandingAmount()}"/></dd></dl></section>
    <c:if test="${not empty payments}"><section class="invoice-payments"><h2>Receipts</h2><table><thead><tr><th>Receipt</th><th>Date</th><th>Method</th><th class="align-right">Amount</th></tr></thead><tbody><c:forEach var="payment" items="${payments}"><tr><td><c:out value="${payment.receiptNumber()}"/></td><td><c:out value="${payment.paidAtDisplay()}"/></td><td><c:out value="${payment.paymentMethod().getDisplayName()}"/></td><td class="align-right">LKR <c:out value="${payment.amount()}"/></td></tr></c:forEach></tbody></table></section></c:if>
    <footer class="invoice-footer"><p>Thank you for choosing Sunrise Dental Clinic.</p><small>Invoice issued by <c:out value="${bill.issuedByName()}"/>. This computer-generated document forms part of the clinic's billing record.</small></footer>
</main>
</body>
</html>

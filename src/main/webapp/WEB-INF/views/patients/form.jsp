<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Register Patient | Sunrise Dental Clinic</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>

<header class="topbar">
    <div>
        <p class="eyebrow">Sunrise Dental Clinic</p>
        <h1>Register Patient</h1>
    </div>

    <a class="secondary-button"
       href="${pageContext.request.contextPath}/patients">
        Back to patients
    </a>
</header>

<main class="page-shell">
    <section class="content-panel form-panel">

        <p class="eyebrow">Patient information</p>
        <h2>Create a new patient record</h2>

        <c:if test="${not empty error}">
            <div class="notice error-notice">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/patients/new">

            <div class="form-grid">
                <div>
                    <label for="firstName">First name *</label>
                    <input id="firstName"
                           name="firstName"
                           maxlength="60"
                           value="${fn:escapeXml(firstName)}"
                           required>
                </div>

                <div>
                    <label for="lastName">Last name *</label>
                    <input id="lastName"
                           name="lastName"
                           maxlength="60"
                           value="${fn:escapeXml(lastName)}"
                           required>
                </div>

                <div>
                    <label for="dateOfBirth">Date of birth</label>
                    <input id="dateOfBirth"
                           name="dateOfBirth"
                           type="date"
                           max="${today}"
                           value="${fn:escapeXml(dateOfBirth)}">
                </div>

                <div>
                    <label for="gender">Gender</label>
                    <select id="gender" name="gender">
                        <option value="">Select</option>
                        <option value="Male"
                        ${gender == 'Male' ? 'selected' : ''}>
                            Male
                        </option>
                        <option value="Female"
                        ${gender == 'Female' ? 'selected' : ''}>
                            Female
                        </option>
                        <option value="Other"
                        ${gender == 'Other' ? 'selected' : ''}>
                            Other
                        </option>
                    </select>
                </div>

                <div>
                    <label for="nicNumber">NIC number</label>
                    <input id="nicNumber"
                           name="nicNumber"
                           maxlength="20"
                           value="${fn:escapeXml(nicNumber)}">
                </div>

                <div>
                    <label for="contactNumber">
                        Contact number *
                    </label>
                    <input id="contactNumber"
                           name="contactNumber"
                           maxlength="20"
                           value="${fn:escapeXml(contactNumber)}"
                           required>
                </div>

                <div>
                    <label for="email">Email address</label>
                    <input id="email"
                           name="email"
                           type="email"
                           maxlength="120"
                           value="${fn:escapeXml(email)}">
                </div>

                <div class="full-width">
                    <label for="address">Address</label>
                    <input id="address"
                           name="address"
                           maxlength="255"
                           value="${fn:escapeXml(address)}">
                </div>

                <div class="full-width">
                    <label for="medicalNotes">Medical notes</label>
                    <textarea id="medicalNotes"
                              name="medicalNotes"
                              rows="4"><c:out value="${medicalNotes}"/></textarea>
                </div>
            </div>

            <div class="form-actions">
                <a class="cancel-link"
                   href="${pageContext.request.contextPath}/patients">
                    Cancel
                </a>

                <button class="submit-button" type="submit">
                    Register patient
                </button>
            </div>
        </form>
    </section>
</main>

</body>
</html>
package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcPatientDao;
import com.sunrisedental.model.Patient;
import com.sunrisedental.service.PatientRegistrationRequest;
import com.sunrisedental.service.PatientService;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/patients/new")
public class PatientRegistrationServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    PatientRegistrationServlet.class.getName()
            );

    private PatientService patientService;
    private AuditService auditService;

    @Override
    public void init() {
        patientService = new PatientService(
                new JdbcPatientDao()
        );
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setAttribute("today", LocalDate.now());

        request.getRequestDispatcher(
                "/WEB-INF/views/patients/form.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        preserveFormValues(request);
        request.setAttribute("today", LocalDate.now());

        try {
            PatientRegistrationRequest registrationRequest =
                    new PatientRegistrationRequest(
                            request.getParameter("firstName"),
                            request.getParameter("lastName"),
                            parseDate(
                                    request.getParameter(
                                            "dateOfBirth"
                                    )
                            ),
                            request.getParameter("gender"),
                            request.getParameter("nicNumber"),
                            request.getParameter("contactNumber"),
                            request.getParameter("email"),
                            request.getParameter("address"),
                            request.getParameter("medicalNotes")
                    );

            Patient patient =
                    patientService.register(registrationRequest);

            AuthenticatedUser actor = (AuthenticatedUser)
                    request.getAttribute("currentUser");
            auditService.record(actor.userId(), "CREATE_PATIENT",
                    "PATIENT", patient.patientNumber(), null);

            String patientNumber = URLEncoder.encode(
                    patient.patientNumber(),
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/patients?created="
                            + patientNumber
            );

        } catch (IllegalArgumentException exception) {
            request.setAttribute(
                    "error",
                    exception.getMessage()
            );

            forwardToForm(request, response);

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to register patient.",
                    exception
            );

            request.setAttribute(
                    "error",
                    "Unable to register the patient. "
                            + "The NIC may already be registered."
            );

            forwardToForm(request, response);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Enter a valid date of birth."
            );
        }
    }

    private void preserveFormValues(
            HttpServletRequest request
    ) {
        String[] fields = {
                "firstName",
                "lastName",
                "dateOfBirth",
                "gender",
                "nicNumber",
                "contactNumber",
                "email",
                "address",
                "medicalNotes"
        };

        for (String field : fields) {
            request.setAttribute(
                    field,
                    request.getParameter(field)
            );
        }
    }

    private void forwardToForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.getRequestDispatcher(
                "/WEB-INF/views/patients/form.jsp"
        ).forward(request, response);
    }
}

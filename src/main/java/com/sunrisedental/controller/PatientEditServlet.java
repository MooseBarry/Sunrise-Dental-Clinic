package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcPatientDao;
import com.sunrisedental.model.Patient;
import com.sunrisedental.service.AuditService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.PatientRegistrationRequest;
import com.sunrisedental.service.PatientService;
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

@WebServlet("/patients/edit")
public class PatientEditServlet extends HttpServlet {
    private PatientService patientService;
    private AuditService auditService;

    @Override
    public void init() {
        patientService = new PatientService(new JdbcPatientDao());
        auditService = new AuditService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            Patient patient = patientService.findById(
                    parseId(request.getParameter("id"))
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Patient was not found."
            ));
            exposePatient(request, patient);
            forward(request, response);
        } catch (IllegalArgumentException | SQLException exception) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Patient was not found."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        long patientId;
        try {
            patientId = parseId(request.getParameter("patientId"));
            Patient patient = patientService.update(
                    patientId,
                    new PatientRegistrationRequest(
                            request.getParameter("firstName"),
                            request.getParameter("lastName"),
                            parseDate(request.getParameter("dateOfBirth")),
                            request.getParameter("gender"),
                            request.getParameter("nicNumber"),
                            request.getParameter("contactNumber"),
                            request.getParameter("email"),
                            request.getParameter("address"),
                            request.getParameter("medicalNotes")
                    )
            );
            AuthenticatedUser actor = (AuthenticatedUser)
                    request.getAttribute("currentUser");
            auditService.record(actor.userId(), "UPDATE_PATIENT",
                    "PATIENT", patient.patientNumber(), null);
            response.sendRedirect(
                    request.getContextPath() + "/patients?updated="
                            + URLEncoder.encode(
                            patient.patientNumber(),
                            StandardCharsets.UTF_8
                    )
            );
        } catch (IllegalArgumentException | SQLException exception) {
            request.setAttribute("error",
                    exception instanceof IllegalArgumentException
                            ? exception.getMessage()
                            : "Patient record could not be updated.");
            request.setAttribute("patientId", request.getParameter("patientId"));
            preserve(request);
            forward(request, response);
        }
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Patient was not found.");
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Enter a valid date of birth.");
        }
    }

    private void exposePatient(HttpServletRequest request, Patient patient) {
        request.setAttribute("patientId", patient.patientId());
        request.setAttribute("patientNumber", patient.patientNumber());
        request.setAttribute("firstName", patient.firstName());
        request.setAttribute("lastName", patient.lastName());
        request.setAttribute("dateOfBirth", patient.dateOfBirth());
        request.setAttribute("gender", patient.gender());
        request.setAttribute("nicNumber", patient.nicNumber());
        request.setAttribute("contactNumber", patient.contactNumber());
        request.setAttribute("email", patient.email());
        request.setAttribute("address", patient.address());
        request.setAttribute("medicalNotes", patient.medicalNotes());
    }

    private void preserve(HttpServletRequest request) {
        for (String field : new String[]{"firstName", "lastName",
                "dateOfBirth", "gender", "nicNumber", "contactNumber",
                "email", "address", "medicalNotes"}) {
            request.setAttribute(field, request.getParameter(field));
        }
    }

    private void forward(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        request.setAttribute("editMode", true);
        request.setAttribute("today", LocalDate.now());
        request.getRequestDispatcher(
                "/WEB-INF/views/patients/form.jsp"
        ).forward(request, response);
    }
}

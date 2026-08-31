package com.sunrisedental.controller;

import com.sunrisedental.dao.ClinicReferenceDao;
import com.sunrisedental.dao.impl.JdbcAppointmentDao;
import com.sunrisedental.dao.impl.JdbcClinicReferenceDao;
import com.sunrisedental.dao.impl.JdbcPatientDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentRegistrationRequest;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.AuthenticatedUser;
import com.sunrisedental.service.PatientService;
import com.sunrisedental.util.SessionConstants;
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
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/appointments/new")
public class AppointmentRegistrationServlet
        extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(
                    AppointmentRegistrationServlet.class.getName()
            );

    private AppointmentService appointmentService;
    private PatientService patientService;
    private ClinicReferenceDao referenceDao;

    @Override
    public void init() {
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao()
        );

        patientService = new PatientService(
                new JdbcPatientDao()
        );

        referenceDao = new JdbcClinicReferenceDao();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        prepareForm(request);

        request.setAttribute(
                "createdAppointmentNumber",
                request.getParameter("created")
        );

        forwardToForm(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        preserveFormValues(request);

        try {
            AuthenticatedUser currentUser =
                    (AuthenticatedUser) request
                            .getSession(false)
                            .getAttribute(
                                    SessionConstants
                                            .AUTHENTICATED_USER
                            );

            AppointmentRegistrationRequest registrationRequest =
                    new AppointmentRegistrationRequest(
                            parsePositiveLong(
                                    request.getParameter(
                                            "patientId"
                                    ),
                                    "Select a patient."
                            ),
                            parsePositiveLong(
                                    request.getParameter(
                                            "dentistId"
                                    ),
                                    "Select a dentist."
                            ),
                            parsePositiveLong(
                                    request.getParameter(
                                            "treatmentId"
                                    ),
                                    "Select a treatment."
                            ),
                            parseDate(
                                    request.getParameter(
                                            "appointmentDate"
                                    )
                            ),
                            parseTime(
                                    request.getParameter(
                                            "startTime"
                                    )
                            ),
                            parseDuration(
                                    request.getParameter(
                                            "durationMinutes"
                                    )
                            ),
                            request.getParameter("reason"),
                            request.getParameter("notes"),
                            currentUser.userId()
                    );

            Appointment appointment =
                    appointmentService.register(
                            registrationRequest
                    );

            String number = URLEncoder.encode(
                    appointment.appointmentNumber(),
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(
                    request.getContextPath()
                            + "/appointments/new?created="
                            + number
            );

        } catch (IllegalArgumentException exception) {
            request.setAttribute(
                    "error",
                    exception.getMessage()
            );

            prepareForm(request);
            forwardToForm(request, response);

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to register appointment.",
                    exception
            );

            request.setAttribute(
                    "error",
                    "Unable to register the appointment."
            );

            prepareForm(request);
            forwardToForm(request, response);
        }
    }

    private void prepareForm(HttpServletRequest request) {
        request.setAttribute("today", LocalDate.now());

        try {
            request.setAttribute(
                    "patients",
                    patientService.getAllPatients()
            );

            request.setAttribute(
                    "dentists",
                    referenceDao.findActiveDentists()
            );

            request.setAttribute(
                    "treatments",
                    referenceDao.findActiveTreatments()
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load appointment form data.",
                    exception
            );

            request.setAttribute("patients", List.of());
            request.setAttribute("dentists", List.of());
            request.setAttribute("treatments", List.of());

            if (request.getAttribute("error") == null) {
                request.setAttribute(
                        "error",
                        "Unable to load appointment options."
                );
            }
        }
    }

    private long parsePositiveLong(
            String value,
            String errorMessage
    ) {
        try {
            long parsed = Long.parseLong(value);

            if (parsed <= 0) {
                throw new NumberFormatException();
            }

            return parsed;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int parseDuration(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Select an appointment duration."
            );
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (
                DateTimeParseException |
                NullPointerException exception
        ) {
            throw new IllegalArgumentException(
                    "Select a valid appointment date."
            );
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (
                DateTimeParseException |
                NullPointerException exception
        ) {
            throw new IllegalArgumentException(
                    "Select a valid appointment time."
            );
        }
    }

    private void preserveFormValues(
            HttpServletRequest request
    ) {
        String[] fields = {
                "patientId",
                "dentistId",
                "treatmentId",
                "appointmentDate",
                "startTime",
                "durationMinutes",
                "reason",
                "notes"
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
                "/WEB-INF/views/appointments/form.jsp"
        ).forward(request, response);
    }
}
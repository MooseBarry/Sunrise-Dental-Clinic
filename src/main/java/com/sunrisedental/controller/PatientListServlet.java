package com.sunrisedental.controller;

import com.sunrisedental.dao.impl.JdbcPatientDao;
import com.sunrisedental.service.PatientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/patients")
public class PatientListServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(PatientListServlet.class.getName());

    private PatientService patientService;

    @Override
    public void init() {
        patientService = new PatientService(
                new JdbcPatientDao()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            request.setAttribute(
                    "patients",
                    patientService.getAllPatients()
            );

            request.setAttribute(
                    "createdPatientNumber",
                    request.getParameter("created")
            );

        } catch (SQLException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Unable to load patients.",
                    exception
            );

            request.setAttribute("patients", List.of());
            request.setAttribute(
                    "error",
                    "Unable to load patient records."
            );
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/patients/list.jsp"
        ).forward(request, response);
    }
}
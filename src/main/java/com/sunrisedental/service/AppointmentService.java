package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import com.sunrisedental.model.AppointmentDetails;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public class AppointmentService {

    private static final DateTimeFormatter NUMBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AppointmentDao appointmentDao;

    public AppointmentService(AppointmentDao appointmentDao) {
        if (appointmentDao == null) {
            throw new IllegalArgumentException(
                    "AppointmentDao must not be null."
            );
        }

        this.appointmentDao = appointmentDao;
    }

    public Appointment register(
            AppointmentRegistrationRequest request
    ) throws SQLException {

        validateRequest(request);

        boolean slotExists =
                appointmentDao.existsDentistSlot(
                        request.dentistId(),
                        request.appointmentDate(),
                        request.startTime()
                );

        if (slotExists) {
            throw new IllegalArgumentException(
                    "The selected dentist already has "
                            + "an appointment at this time."
            );
        }

        Appointment appointment = new Appointment(
                0,
                generateAppointmentNumber(request),
                request.patientId(),
                request.dentistId(),
                request.treatmentId(),
                request.appointmentDate(),
                request.startTime(),
                request.durationMinutes(),
                normalize(request.reason(), 255),
                AppointmentStatus.SCHEDULED,
                normalize(request.notes(), 2000),
                request.createdBy()
        );

        long appointmentId =
                appointmentDao.create(appointment);

        return new Appointment(
                appointmentId,
                appointment.appointmentNumber(),
                appointment.patientId(),
                appointment.dentistId(),
                appointment.treatmentId(),
                appointment.appointmentDate(),
                appointment.startTime(),
                appointment.durationMinutes(),
                appointment.reason(),
                appointment.status(),
                appointment.notes(),
                appointment.createdBy()
        );
    }
    public List<AppointmentDetails> getAllAppointments()
            throws SQLException {

        return appointmentDao.findAll();
    }

    public Optional<AppointmentDetails>
    findByAppointmentNumber(
            String appointmentNumber
    ) throws SQLException {

        if (appointmentNumber == null
                || appointmentNumber.isBlank()) {
            return Optional.empty();
        }

        return appointmentDao.findByAppointmentNumber(
                appointmentNumber.trim()
        );
    }

    private void validateRequest(
            AppointmentRegistrationRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Appointment information is required."
            );
        }

        if (request.patientId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a patient."
            );
        }

        if (request.dentistId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a dentist."
            );
        }

        if (request.treatmentId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a treatment."
            );
        }

        if (request.createdBy() <= 0) {
            throw new IllegalArgumentException(
                    "A signed-in staff member is required."
            );
        }

        if (request.appointmentDate() == null
                || request.startTime() == null) {
            throw new IllegalArgumentException(
                    "Appointment date and time are required."
            );
        }

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(
                        request.appointmentDate(),
                        request.startTime()
                );

        if (!appointmentDateTime.isAfter(
                LocalDateTime.now()
        )) {
            throw new IllegalArgumentException(
                    "Appointment time must be in the future."
            );
        }

        if (request.durationMinutes() < 15
                || request.durationMinutes() > 180) {
            throw new IllegalArgumentException(
                    "Duration must be between 15 and 180 minutes."
            );
        }
    }

    private String generateAppointmentNumber(
            AppointmentRegistrationRequest request
    ) {
        String date = request.appointmentDate()
                .format(NUMBER_DATE_FORMAT);

        String identifier = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase(Locale.ROOT);

        return "APT-" + date + "-" + identifier;
    }

    private String normalize(
            String value,
            int maximumLength
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.length() > maximumLength) {
            throw new IllegalArgumentException(
                    "A value exceeds the allowed length."
            );
        }

        return normalized;
    }
}
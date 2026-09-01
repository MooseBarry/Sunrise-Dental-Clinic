package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.AppointmentStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class AppointmentService {

    private static final DateTimeFormatter
            NUMBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AppointmentDao appointmentDao;

    public AppointmentService(
            AppointmentDao appointmentDao
    ) {
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

        boolean overlapExists =
                appointmentDao.hasOverlappingAppointment(
                        request.dentistId(),
                        request.appointmentDate(),
                        request.startTime(),
                        request.durationMinutes()
                );

        if (overlapExists) {
            throw new IllegalArgumentException(
                    "The selected time overlaps with another "
                            + "appointment for this dentist."
            );
        }

        Appointment appointment =
                new Appointment(
                        0,
                        generateAppointmentNumber(request),
                        request.patientId(),
                        request.dentistId(),
                        request.treatmentId(),
                        request.appointmentDate(),
                        request.startTime(),
                        request.durationMinutes(),
                        normalize(
                                request.reason(),
                                255
                        ),
                        AppointmentStatus.SCHEDULED,
                        normalize(
                                request.notes(),
                                2000
                        ),
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
                normalizeAppointmentNumber(
                        appointmentNumber
                )
        );
    }

    public AppointmentDetails changeStatus(
            String appointmentNumber,
            AppointmentStatus newStatus
    ) throws SQLException {

        if (appointmentNumber == null
                || appointmentNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Appointment number is required."
            );
        }

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Select a valid appointment status."
            );
        }

        String normalizedNumber =
                normalizeAppointmentNumber(
                        appointmentNumber
                );

        AppointmentDetails current =
                appointmentDao.findByAppointmentNumber(
                        normalizedNumber
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "Appointment was not found."
                        )
                );

        if (current.status() == newStatus) {
            return current;
        }

        if (current.status()
                != AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "A completed, cancelled or no-show "
                            + "appointment cannot be changed."
            );
        }

        if (newStatus == AppointmentStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "Select a final appointment status."
            );
        }

        boolean updated =
                appointmentDao.updateStatus(
                        current.appointmentNumber(),
                        newStatus
                );

        if (!updated) {
            throw new SQLException(
                    "Appointment status was not updated."
            );
        }

        return appointmentDao.findByAppointmentNumber(
                current.appointmentNumber()
        ).orElseThrow(
                () -> new SQLException(
                        "Updated appointment could not be loaded."
                )
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
                || request.durationMinutes() > 180
                || request.durationMinutes() % 15 != 0) {
            throw new IllegalArgumentException(
                    "Duration must be between 15 and 180 "
                            + "minutes in 15-minute intervals."
            );
        }

        LocalDateTime appointmentEnd =
                appointmentDateTime.plusMinutes(
                        request.durationMinutes()
                );

        if (!appointmentEnd.toLocalDate().equals(
                request.appointmentDate()
        )) {
            throw new IllegalArgumentException(
                    "An appointment cannot continue "
                            + "into the following day."
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

    private String normalizeAppointmentNumber(
            String appointmentNumber
    ) {
        return appointmentNumber
                .trim()
                .toUpperCase(Locale.ROOT);
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
package com.sunrisedental.service;

import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.model.Patient;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class PatientService {
    private final PatientDao patientDao;

    public PatientService(PatientDao patientDao) {
        if (patientDao == null) {
            throw new IllegalArgumentException(
                    "PatientDao must not be null."
            );
        }
        this.patientDao = patientDao;
    }

    public Patient register(PatientRegistrationRequest request)
            throws SQLException {
        Patient patient = validate(
                request,
                0,
                generatePatientNumber()
        );
        long id = patientDao.create(patient);
        return copyWithId(patient, id);
    }

    public Patient update(
            long patientId,
            PatientRegistrationRequest request
    ) throws SQLException {
        Patient existing = patientDao.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient was not found."
                ));
        Patient updated = validate(
                request,
                patientId,
                existing.patientNumber()
        );
        if (!patientDao.update(updated)) {
            throw new SQLException("Patient record was not updated.");
        }
        return updated;
    }

    public List<Patient> getAllPatients() throws SQLException {
        return patientDao.findAll();
    }

    public List<Patient> searchPatients(String query)
            throws SQLException {
        if (query == null || query.isBlank()) {
            return getAllPatients();
        }
        String normalized = query.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Search value is too long.");
        }
        return patientDao.search(normalized);
    }

    public Optional<Patient> findByPatientNumber(String patientNumber)
            throws SQLException {
        if (patientNumber == null || patientNumber.isBlank()) {
            return Optional.empty();
        }
        return patientDao.findByPatientNumber(patientNumber.trim());
    }

    public Optional<Patient> findById(long patientId)
            throws SQLException {
        return patientId <= 0
                ? Optional.empty()
                : patientDao.findById(patientId);
    }

    private Patient validate(
            PatientRegistrationRequest request,
            long patientId,
            String patientNumber
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Patient information is required."
            );
        }

        String firstName = requireText(
                request.firstName(), "First name", 60
        );
        String lastName = requireText(
                request.lastName(), "Last name", 60
        );
        String contactNumber = requireText(
                request.contactNumber(), "Contact number", 20
        );
        if (!contactNumber.matches("[0-9+()\\-\\s]{7,20}")) {
            throw new IllegalArgumentException(
                    "Enter a valid contact number."
            );
        }
        if (request.dateOfBirth() != null
                && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Date of birth cannot be in the future."
            );
        }

        String email = optionalText(request.email(), 120);
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException(
                        "Enter a valid email address."
                );
            }
        }

        return new Patient(
                patientId,
                patientNumber,
                firstName,
                lastName,
                request.dateOfBirth(),
                optionalText(request.gender(), 20),
                optionalText(request.nicNumber(), 20),
                contactNumber,
                email,
                optionalText(request.address(), 255),
                optionalText(request.medicalNotes(), 2000)
        );
    }

    private Patient copyWithId(Patient patient, long id) {
        return new Patient(
                id,
                patient.patientNumber(),
                patient.firstName(),
                patient.lastName(),
                patient.dateOfBirth(),
                patient.gender(),
                patient.nicNumber(),
                patient.contactNumber(),
                patient.email(),
                patient.address(),
                patient.medicalNotes()
        );
    }

    private String generatePatientNumber() {
        String identifier = UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return "PAT-" + identifier;
    }

    private String requireText(
            String value,
            String fieldName,
            int maximumLength
    ) {
        String normalized = optionalText(value, maximumLength);
        if (normalized == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }
        return normalized;
    }

    private String optionalText(String value, int maximumLength) {
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

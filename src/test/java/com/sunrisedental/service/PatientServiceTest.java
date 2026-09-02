package com.sunrisedental.service;

import com.sunrisedental.dao.PatientDao;
import com.sunrisedental.model.Patient;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PatientServiceTest {

    @Test
    void shouldRegisterValidPatient() throws SQLException {
        FakePatientDao patientDao = new FakePatientDao();
        PatientService service = new PatientService(patientDao);

        Patient patient = service.register(
                validRequest()
        );

        assertEquals(42L, patient.patientId());
        assertTrue(
                patient.patientNumber().startsWith("PAT-")
        );
        assertEquals("Nimal", patient.firstName());
        assertEquals("Perera", patient.lastName());
        assertNotNull(patientDao.savedPatient);
    }

    @Test
    void shouldRejectMissingFirstName() {
        PatientService service =
                new PatientService(new FakePatientDao());

        PatientRegistrationRequest request =
                new PatientRegistrationRequest(
                        " ",
                        "Perera",
                        LocalDate.of(1995, 5, 10),
                        "Male",
                        "951234567V",
                        "0771234567",
                        "nimal@example.com",
                        "Colombo",
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void shouldRejectInvalidContactNumber() {
        PatientService service =
                new PatientService(new FakePatientDao());

        PatientRegistrationRequest request =
                new PatientRegistrationRequest(
                        "Nimal",
                        "Perera",
                        null,
                        null,
                        null,
                        "abc",
                        null,
                        null,
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void shouldRejectFutureDateOfBirth() {
        PatientService service =
                new PatientService(new FakePatientDao());

        PatientRegistrationRequest request =
                new PatientRegistrationRequest(
                        "Nimal",
                        "Perera",
                        LocalDate.now().plusDays(1),
                        null,
                        null,
                        "0771234567",
                        null,
                        null,
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void shouldRejectInvalidEmailAddress() {
        PatientService service =
                new PatientService(new FakePatientDao());

        PatientRegistrationRequest request =
                new PatientRegistrationRequest(
                        "Nimal",
                        "Perera",
                        null,
                        null,
                        null,
                        "0771234567",
                        "invalid-email",
                        null,
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    private PatientRegistrationRequest validRequest() {
        return new PatientRegistrationRequest(
                " Nimal ",
                " Perera ",
                LocalDate.of(1995, 5, 10),
                "Male",
                "951234567V",
                "0771234567",
                "nimal@example.com",
                "Colombo",
                "No known allergies"
        );
    }

    private static class FakePatientDao
            implements PatientDao {

        private Patient savedPatient;

        @Override
        public long create(Patient patient) {
            savedPatient = patient;
            return 42L;
        }

        @Override
        public List<Patient> findAll() {
            if (savedPatient == null) {
                return List.of();
            }

            return List.of(savedPatient);
        }

        @Override
        public List<Patient> search(String query) {
            return findAll();
        }

        @Override
        public Optional<Patient> findById(long patientId) {
            return Optional.ofNullable(savedPatient);
        }

        @Override
        public Optional<Patient> findByPatientNumber(
                String patientNumber
        ) {
            return Optional.ofNullable(savedPatient);
        }

        @Override
        public boolean update(Patient patient) {
            savedPatient = patient;
            return true;
        }
    }
}

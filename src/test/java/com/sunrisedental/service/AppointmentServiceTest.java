package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import org.junit.jupiter.api.Test;
import com.sunrisedental.model.AppointmentDetails;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    @Test
    void shouldRegisterValidAppointment() throws Exception {
        FakeAppointmentDao dao = new FakeAppointmentDao();
        AppointmentService service =
                new AppointmentService(dao);

        Appointment appointment =
                service.register(validRequest());

        assertEquals(77L, appointment.appointmentId());
        assertTrue(
                appointment.appointmentNumber()
                        .startsWith("APT-")
        );
        assertEquals(
                AppointmentStatus.SCHEDULED,
                appointment.status()
        );
        assertNotNull(dao.savedAppointment);
    }

    @Test
    void shouldRejectPastAppointment() {
        AppointmentService service =
                new AppointmentService(
                        new FakeAppointmentDao()
                );

        AppointmentRegistrationRequest request =
                new AppointmentRegistrationRequest(
                        1,
                        1,
                        1,
                        LocalDate.now().minusDays(1),
                        LocalTime.of(10, 0),
                        30,
                        null,
                        null,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void shouldRejectUnavailableDentistSlot() {
        FakeAppointmentDao dao = new FakeAppointmentDao();
        dao.slotTaken = true;

        AppointmentService service =
                new AppointmentService(dao);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.register(validRequest())
                );

        assertTrue(
                exception.getMessage()
                        .contains("already has an appointment")
        );
    }

    @Test
    void shouldRejectInvalidDuration() {
        AppointmentService service =
                new AppointmentService(
                        new FakeAppointmentDao()
                );

        AppointmentRegistrationRequest request =
                new AppointmentRegistrationRequest(
                        1,
                        1,
                        1,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0),
                        5,
                        null,
                        null,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }

    @Test
    void shouldRejectMissingPatient() {
        AppointmentService service =
                new AppointmentService(
                        new FakeAppointmentDao()
                );

        AppointmentRegistrationRequest request =
                new AppointmentRegistrationRequest(
                        0,
                        1,
                        1,
                        LocalDate.now().plusDays(1),
                        LocalTime.of(10, 0),
                        30,
                        null,
                        null,
                        1
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(request)
        );
    }
    @Test
    void shouldRejectBlankAppointmentSearch() throws Exception {
        AppointmentService service =
                new AppointmentService(
                        new FakeAppointmentDao()
                );

        assertTrue(
                service.findByAppointmentNumber(" ").isEmpty()
        );
    }
    @Test
    void shouldCancelScheduledAppointment() throws Exception {
        FakeAppointmentDao dao = new FakeAppointmentDao();
        dao.storedDetails = sampleDetails(
                AppointmentStatus.SCHEDULED
        );

        AppointmentService service =
                new AppointmentService(dao);

        AppointmentDetails result = service.changeStatus(
                "APT-TEST-001",
                AppointmentStatus.CANCELLED
        );

        assertEquals(
                AppointmentStatus.CANCELLED,
                result.status()
        );
    }

    @Test
    void shouldRejectChangingCompletedAppointment() {
        FakeAppointmentDao dao = new FakeAppointmentDao();
        dao.storedDetails = sampleDetails(
                AppointmentStatus.COMPLETED
        );

        AppointmentService service =
                new AppointmentService(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.changeStatus(
                        "APT-TEST-001",
                        AppointmentStatus.CANCELLED
                )
        );
    }
    private AppointmentDetails sampleDetails(
            AppointmentStatus status
    ) {
        return new AppointmentDetails(
                1L,
                "APT-TEST-001",
                "PAT-TEST-001",
                "Test Patient",
                "Colombo",
                "0771234567",
                "Dr. Kasun Perera",
                "SLDC-1001",
                "Dental Cleaning",
                1,
                new BigDecimal("5000.00"),
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                30,
                "Dental examination",
                status,
                null,
                "System Administrator",
                LocalDateTime.now()
        );
    }

    private AppointmentRegistrationRequest validRequest() {
        return new AppointmentRegistrationRequest(
                1,
                1,
                1,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                30,
                "Dental examination",
                "First appointment",
                1
        );
    }

    private static class FakeAppointmentDao
            implements AppointmentDao {

        private boolean slotTaken;
        private Appointment savedAppointment;

        @Override
        public long create(Appointment appointment) {
            savedAppointment = appointment;
            return 77L;
        }

        @Override
        public boolean existsDentistSlot(
                long dentistId,
                LocalDate appointmentDate,
                LocalTime startTime
        ) {
            return slotTaken;
        }
        @Override
        public List<AppointmentDetails> findAll() {
            if (storedDetails == null) {
                return List.of();
            }

            return List.of(storedDetails);
        }

        @Override
        public Optional<AppointmentDetails>
        findByAppointmentNumber(
                String appointmentNumber
        ) {
            if (storedDetails == null
                    || !storedDetails.appointmentNumber()
                    .equals(appointmentNumber)) {
                return Optional.empty();
            }

            return Optional.of(storedDetails);
        }

        @Override
        public boolean updateStatus(
                String appointmentNumber,
                AppointmentStatus status
        ) {
            if (storedDetails == null
                    || !storedDetails.appointmentNumber()
                    .equals(appointmentNumber)) {
                return false;
            }

            storedDetails = new AppointmentDetails(
                    storedDetails.appointmentId(),
                    storedDetails.appointmentNumber(),
                    storedDetails.patientNumber(),
                    storedDetails.patientName(),
                    storedDetails.patientAddress(),
                    storedDetails.patientContact(),
                    storedDetails.dentistName(),
                    storedDetails.dentistRegistrationNumber(),
                    storedDetails.treatmentName(),
                    storedDetails.treatmentQuantity(),
                    storedDetails.chargedFee(),
                    storedDetails.appointmentDate(),
                    storedDetails.startTime(),
                    storedDetails.durationMinutes(),
                    storedDetails.reason(),
                    status,
                    storedDetails.notes(),
                    storedDetails.createdByName(),
                    storedDetails.createdAt()
            );

            return true;
        }
        private AppointmentDetails storedDetails;
    }
}
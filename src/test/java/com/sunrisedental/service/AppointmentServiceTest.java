package com.sunrisedental.service;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

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
    }
}
package com.sunrisedental.service;

import com.sunrisedental.dao.StaffDao;
import com.sunrisedental.model.Role;
import com.sunrisedental.model.StaffAccount;
import com.sunrisedental.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaffServiceTest {
    @Test
    void shouldCreateSecureCashierAccount() throws Exception {
        FakeStaffDao dao = new FakeStaffDao();
        StaffService service = new StaffService(dao);

        long id = service.register(new StaffRegistrationRequest(
                " cashier.one ",
                "SecurePass1!",
                "Clinic Cashier",
                "cashier@example.com",
                "0771234567",
                Role.CASHIER,
                null,
                null,
                null
        ));

        assertEquals(55L, id);
        assertNotNull(dao.created);
        assertEquals("cashier.one", dao.created.getUsername());
        assertNotEquals("SecurePass1!", dao.created.getPasswordHash());
    }

    @Test
    void shouldRejectWeakPassword() {
        StaffService service = new StaffService(new FakeStaffDao());
        assertThrows(IllegalArgumentException.class, () ->
                service.register(new StaffRegistrationRequest(
                        "cashier.one", "password", "Cashier",
                        null, null, Role.CASHIER,
                        null, null, null
                ))
        );
    }

    @Test
    void shouldPreventSelfDeactivation() {
        StaffService service = new StaffService(new FakeStaffDao());
        assertThrows(IllegalArgumentException.class,
                () -> service.setActive(10L, false, 10L));
    }

    @Test
    void shouldCreateDentistWithProfessionalProfile() throws Exception {
        FakeStaffDao dao = new FakeStaffDao();
        StaffService service = new StaffService(dao);

        service.register(new StaffRegistrationRequest(
                "dr.silva",
                "SecurePass1!",
                "Dr. Nimal Silva",
                "nimal.silva@example.com",
                "0771112233",
                Role.DENTIST,
                "sldc-2002",
                "Orthodontics",
                new BigDecimal("3500")
        ));

        assertEquals("SLDC-2002", dao.registrationNumber);
        assertEquals("Orthodontics", dao.specialization);
        assertEquals(new BigDecimal("3500.00"), dao.consultationFee);
    }

    @Test
    void shouldRequireDentistRegistrationDetails() {
        StaffService service = new StaffService(new FakeStaffDao());

        assertThrows(IllegalArgumentException.class, () ->
                service.register(new StaffRegistrationRequest(
                        "dr.silva", "SecurePass1!", "Dr. Silva",
                        null, null, Role.DENTIST,
                        null, "General Dentistry",
                        new BigDecimal("3000")
                ))
        );
    }

    private static class FakeStaffDao implements StaffDao {
        private User created;
        private String registrationNumber;
        private String specialization;
        private BigDecimal consultationFee;

        @Override public List<StaffAccount> findAll() { return List.of(); }
        @Override public boolean usernameExists(String value) { return false; }
        @Override public boolean emailExists(String value) { return false; }
        @Override
        public long create(
                User user,
                String registrationNumber,
                String specialization,
                java.math.BigDecimal consultationFee
        ) {
            created = user;
            this.registrationNumber = registrationNumber;
            this.specialization = specialization;
            this.consultationFee = consultationFee;
            return 55L;
        }
        @Override public boolean updateActive(long id, boolean active) { return true; }
        @Override public boolean updatePassword(long id, String hash) { return true; }
    }
}

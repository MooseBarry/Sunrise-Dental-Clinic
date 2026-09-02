package com.sunrisedental.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionTest {
    @Test
    void administratorShouldHaveEveryPermission() {
        for (Permission permission : Permission.values()) {
            assertTrue(Role.ADMIN.allows(permission));
        }
    }

    @Test
    void receptionistShouldManageSchedulingButNotBilling() {
        assertTrue(Role.RECEPTIONIST.allows(Permission.MANAGE_PATIENTS));
        assertTrue(Role.RECEPTIONIST.allows(Permission.MANAGE_APPOINTMENTS));
        assertFalse(Role.RECEPTIONIST.allows(Permission.MANAGE_BILLING));
        assertFalse(Role.RECEPTIONIST.allows(Permission.VIEW_REPORTS));
    }

    @Test
    void cashierShouldManageBillingOnly() {
        assertTrue(Role.CASHIER.allows(Permission.MANAGE_BILLING));
        assertFalse(Role.CASHIER.allows(Permission.MANAGE_PATIENTS));
        assertFalse(Role.CASHIER.allows(Permission.MANAGE_APPOINTMENTS));
    }

    @Test
    void dentistShouldUpdateStatusWithoutRegisteringAppointments() {
        assertTrue(Role.DENTIST.allows(
                Permission.UPDATE_APPOINTMENT_STATUS
        ));
        assertFalse(Role.DENTIST.allows(
                Permission.MANAGE_APPOINTMENTS
        ));
    }
}

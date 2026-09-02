package com.sunrisedental.service;

import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.BillDetails;
import com.sunrisedental.model.NotificationType;
import com.sunrisedental.model.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationCoordinator {
    private static final Logger LOGGER = Logger.getLogger(
            NotificationCoordinator.class.getName()
    );

    private final NotificationService notificationService;
    private final EmailService emailService;

    public NotificationCoordinator(
            NotificationService notificationService,
            EmailService emailService
    ) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public void appointmentCreated(AppointmentDetails appointment) {
        publish(
                EnumSet.of(Role.ADMIN, Role.RECEPTIONIST, Role.DENTIST),
                NotificationType.APPOINTMENT,
                "New appointment registered",
                appointment.appointmentNumber() + " for "
                        + appointment.patientName() + " with "
                        + appointment.dentistName() + ".",
                "APPOINTMENT",
                appointment.appointmentNumber()
        );

        emailService.send(
                appointment.patientEmail(),
                "Appointment confirmation",
                "Dear " + appointment.patientName() + ",\n\n"
                        + "Your appointment "
                        + appointment.appointmentNumber()
                        + " is scheduled for "
                        + appointment.appointmentDate() + " at "
                        + appointment.startTime() + " with "
                        + appointment.dentistName() + ".\n\n"
                        + "Please contact the clinic if you need assistance."
        );
    }

    public void appointmentStatusChanged(AppointmentDetails appointment) {
        publish(
                EnumSet.of(Role.ADMIN, Role.RECEPTIONIST, Role.DENTIST),
                NotificationType.APPOINTMENT,
                "Appointment status updated",
                appointment.appointmentNumber() + " is now "
                        + appointment.status().name().replace('_', ' ') + ".",
                "APPOINTMENT",
                appointment.appointmentNumber()
        );

        emailService.send(
                appointment.patientEmail(),
                "Appointment update",
                "Dear " + appointment.patientName() + ",\n\n"
                        + "Your appointment "
                        + appointment.appointmentNumber()
                        + " is now "
                        + appointment.status().name().replace('_', ' ')
                        + ".\n\nSunrise Dental Clinic"
        );
    }

    public void billCreated(BillDetails bill) {
        publish(
                EnumSet.of(Role.ADMIN, Role.CASHIER),
                NotificationType.BILLING,
                "Invoice generated",
                bill.billNumber() + " generated for "
                        + bill.patientName() + " (LKR "
                        + bill.totalAmount().toPlainString() + ").",
                "BILL",
                bill.billNumber()
        );
        emailService.send(
                bill.patientEmail(),
                "Invoice " + bill.billNumber(),
                "Dear " + bill.patientName() + ",\n\n"
                        + "Your invoice total is LKR "
                        + bill.totalAmount().toPlainString()
                        + ". Invoice reference: " + bill.billNumber()
                        + ".\n\nSunrise Dental Clinic"
        );
    }

    public void paymentRecorded(BillDetails bill, BigDecimal amount) {
        publish(
                EnumSet.of(Role.ADMIN, Role.CASHIER),
                NotificationType.PAYMENT,
                "Payment recorded",
                "LKR " + amount.toPlainString() + " received for "
                        + bill.billNumber() + ". Balance: LKR "
                        + bill.outstandingAmount().toPlainString() + ".",
                "BILL",
                bill.billNumber()
        );
        emailService.send(
                bill.patientEmail(),
                "Payment received",
                "Dear " + bill.patientName() + ",\n\n"
                        + "We received LKR " + amount.toPlainString()
                        + " for invoice " + bill.billNumber()
                        + ". Remaining balance: LKR "
                        + bill.outstandingAmount().toPlainString()
                        + ".\n\nThank you,\nSunrise Dental Clinic"
        );
    }

    private void publish(
            EnumSet<Role> roles,
            NotificationType type,
            String title,
            String message,
            String referenceType,
            String referenceValue
    ) {
        try {
            notificationService.publish(
                    roles, type, title, message,
                    referenceType, referenceValue
            );
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING,
                    "Internal notification could not be stored.",
                    exception);
        }
    }
}

package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentDetails;
import com.sunrisedental.model.AppointmentStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao {

    long create(Appointment appointment) throws SQLException;

    boolean hasOverlappingAppointment(
            long dentistId,
            LocalDate appointmentDate,
            LocalTime startTime,
            int durationMinutes
    ) throws SQLException;

    List<AppointmentDetails> findAll() throws SQLException;

    Optional<AppointmentDetails> findByAppointmentNumber(
            String appointmentNumber
    ) throws SQLException;

    boolean updateStatus(
            String appointmentNumber,
            AppointmentStatus status
    ) throws SQLException;
}
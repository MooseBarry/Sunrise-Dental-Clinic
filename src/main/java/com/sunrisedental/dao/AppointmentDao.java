package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentDao {

    long create(Appointment appointment) throws SQLException;

    boolean existsDentistSlot(
            long dentistId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) throws SQLException;
}
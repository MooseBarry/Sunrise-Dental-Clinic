package com.sunrisedental.dao;

import com.sunrisedental.model.Patient;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PatientDao {

    long create(Patient patient) throws SQLException;

    List<Patient> findAll() throws SQLException;

    List<Patient> search(String query) throws SQLException;

    Optional<Patient> findById(long patientId) throws SQLException;

    Optional<Patient> findByPatientNumber(
            String patientNumber
    ) throws SQLException;

    boolean update(Patient patient) throws SQLException;
}

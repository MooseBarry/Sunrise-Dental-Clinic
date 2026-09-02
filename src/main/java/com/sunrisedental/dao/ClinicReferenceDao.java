package com.sunrisedental.dao;

import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Treatment;

import java.sql.SQLException;
import java.util.List;

public interface ClinicReferenceDao {

    List<Dentist> findActiveDentists() throws SQLException;

    List<Treatment> findActiveTreatments() throws SQLException;
}
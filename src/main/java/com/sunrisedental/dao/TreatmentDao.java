package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TreatmentDao {
    List<Treatment> findAll() throws SQLException;

    Optional<Treatment> findById(long treatmentId) throws SQLException;

    boolean codeExists(String treatmentCode) throws SQLException;

    long create(Treatment treatment) throws SQLException;

    boolean update(Treatment treatment) throws SQLException;

    boolean updateActive(long treatmentId, boolean active)
            throws SQLException;
}

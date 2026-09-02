package com.sunrisedental.dao.impl;

import com.sunrisedental.config.DatabaseConfig;
import com.sunrisedental.dao.ClinicReferenceDao;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Treatment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcClinicReferenceDao
        implements ClinicReferenceDao {

    private static final String SELECT_DENTISTS =
            "SELECT d.dentist_id, u.full_name, " +
                    "d.registration_number, d.specialization, " +
                    "d.consultation_fee " +
                    "FROM dentists d " +
                    "INNER JOIN users u ON d.user_id = u.user_id " +
                    "WHERE d.active = TRUE AND u.active = TRUE " +
                    "ORDER BY u.full_name";

    private static final String SELECT_TREATMENTS =
            "SELECT treatment_id, treatment_code, " +
                    "treatment_name, description, standard_fee " +
                    "FROM treatments " +
                    "WHERE active = TRUE " +
                    "ORDER BY treatment_name";

    @Override
    public List<Dentist> findActiveDentists()
            throws SQLException {

        List<Dentist> dentists = new ArrayList<>();

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(SELECT_DENTISTS);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                dentists.add(
                        new Dentist(
                                resultSet.getLong("dentist_id"),
                                resultSet.getString("full_name"),
                                resultSet.getString(
                                        "registration_number"
                                ),
                                resultSet.getString(
                                        "specialization"
                                ),
                                resultSet.getBigDecimal(
                                        "consultation_fee"
                                )
                        )
                );
            }
        }

        return dentists;
    }

    @Override
    public List<Treatment> findActiveTreatments()
            throws SQLException {

        List<Treatment> treatments = new ArrayList<>();

        try (
                Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                SELECT_TREATMENTS
                        );
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                treatments.add(
                        new Treatment(
                                resultSet.getLong("treatment_id"),
                                resultSet.getString(
                                        "treatment_code"
                                ),
                                resultSet.getString(
                                        "treatment_name"
                                ),
                                resultSet.getString("description"),
                                resultSet.getBigDecimal(
                                        "standard_fee"
                                ),
                                true
                        )
                );
            }
        }

        return treatments;
    }
}

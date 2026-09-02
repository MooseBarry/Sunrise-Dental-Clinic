package com.sunrisedental.service;

import com.sunrisedental.dao.TreatmentDao;
import com.sunrisedental.model.Treatment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class TreatmentService {
    private final TreatmentDao treatmentDao;

    public TreatmentService(TreatmentDao treatmentDao) {
        this.treatmentDao = treatmentDao;
    }

    public List<Treatment> getAll() throws SQLException {
        return treatmentDao.findAll();
    }

    public long save(
            Long treatmentId,
            String code,
            String name,
            String description,
            BigDecimal fee
    ) throws SQLException {
        String normalizedCode = require(code, "Treatment code", 20)
                .toUpperCase(Locale.ROOT);
        if (!normalizedCode.matches("[A-Z0-9-]{3,20}")) {
            throw new IllegalArgumentException(
                    "Treatment code may contain letters, numbers and dashes."
            );
        }
        String normalizedName = require(name, "Treatment name", 100);
        String normalizedDescription = optional(description, 255);
        if (fee == null || fee.signum() < 0) {
            throw new IllegalArgumentException(
                    "Standard fee must be zero or greater."
            );
        }
        BigDecimal normalizedFee = fee.setScale(2, RoundingMode.HALF_UP);

        if (treatmentId == null) {
            if (treatmentDao.codeExists(normalizedCode)) {
                throw new IllegalArgumentException(
                        "That treatment code already exists."
                );
            }
            return treatmentDao.create(new Treatment(
                    0, normalizedCode, normalizedName,
                    normalizedDescription, normalizedFee, true
            ));
        }

        Treatment current = treatmentDao.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Treatment was not found."
                ));
        if (!current.treatmentCode().equals(normalizedCode)
                && treatmentDao.codeExists(normalizedCode)) {
            throw new IllegalArgumentException(
                    "That treatment code already exists."
            );
        }
        Treatment updated = new Treatment(
                treatmentId, normalizedCode, normalizedName,
                normalizedDescription, normalizedFee, current.active()
        );
        if (!treatmentDao.update(updated)) {
            throw new IllegalArgumentException("Treatment was not found.");
        }
        return treatmentId;
    }

    public void setActive(long treatmentId, boolean active)
            throws SQLException {
        if (treatmentId <= 0
                || !treatmentDao.updateActive(treatmentId, active)) {
            throw new IllegalArgumentException("Treatment was not found.");
        }
    }

    private String require(String value, String name, int max) {
        String normalized = optional(value, max);
        if (normalized == null) {
            throw new IllegalArgumentException(name + " is required.");
        }
        return normalized;
    }

    private String optional(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > max) {
            throw new IllegalArgumentException(
                    "A treatment value exceeds the allowed length."
            );
        }
        return normalized;
    }
}

package com.sunrisedental.service;

import com.sunrisedental.dao.ReportDao;
import com.sunrisedental.model.ManagementReport;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReportService {
    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public ManagementReport generate(LocalDate from, LocalDate to)
            throws SQLException {
        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Select both report dates."
            );
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after the to date."
            );
        }
        if (to.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Report end date cannot be in the future."
            );
        }
        if (ChronoUnit.DAYS.between(from, to) > 366) {
            throw new IllegalArgumentException(
                    "Select a report period of 366 days or less."
            );
        }
        return reportDao.generate(from, to);
    }
}

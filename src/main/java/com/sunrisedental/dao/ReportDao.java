package com.sunrisedental.dao;

import com.sunrisedental.model.ManagementReport;

import java.sql.SQLException;
import java.time.LocalDate;

public interface ReportDao {
    ManagementReport generate(LocalDate fromDate, LocalDate toDate)
            throws SQLException;
}

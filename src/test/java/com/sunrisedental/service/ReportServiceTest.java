package com.sunrisedental.service;

import com.sunrisedental.dao.ReportDao;
import com.sunrisedental.model.ManagementReport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportServiceTest {
    @Test
    void shouldRejectReversedDates() {
        ReportService service = new ReportService(new FakeReportDao());
        assertThrows(IllegalArgumentException.class, () ->
                service.generate(LocalDate.now(), LocalDate.now().minusDays(1))
        );
    }

    @Test
    void shouldRejectPeriodLongerThanOneYear() {
        ReportService service = new ReportService(new FakeReportDao());
        assertThrows(IllegalArgumentException.class, () ->
                service.generate(LocalDate.now().minusDays(367), LocalDate.now())
        );
    }

    private static class FakeReportDao implements ReportDao {
        @Override
        public ManagementReport generate(LocalDate from, LocalDate to) {
            return new ManagementReport(from, to, 0, 0, 0, 0, 0, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), List.of());
        }
    }
}

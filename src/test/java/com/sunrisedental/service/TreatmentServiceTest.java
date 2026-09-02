package com.sunrisedental.service;

import com.sunrisedental.dao.TreatmentDao;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentServiceTest {
    @Test
    void shouldCreateTreatmentWithNormalisedMoney() throws Exception {
        FakeTreatmentDao dao = new FakeTreatmentDao();
        TreatmentService service = new TreatmentService(dao);
        long id = service.save(null, "trt-100", "Whitening",
                "Professional whitening", new BigDecimal("12500"));
        assertEquals(9L, id);
        assertEquals("TRT-100", dao.saved.treatmentCode());
        assertEquals(new BigDecimal("12500.00"), dao.saved.standardFee());
    }

    @Test
    void shouldRejectNegativeFee() {
        TreatmentService service = new TreatmentService(
                new FakeTreatmentDao()
        );
        assertThrows(IllegalArgumentException.class, () ->
                service.save(null, "TRT-100", "Whitening", null,
                        new BigDecimal("-1"))
        );
    }

    private static class FakeTreatmentDao implements TreatmentDao {
        private Treatment saved;
        @Override public List<Treatment> findAll() { return List.of(); }
        @Override public Optional<Treatment> findById(long id) { return Optional.ofNullable(saved); }
        @Override public boolean codeExists(String code) { return false; }
        @Override public long create(Treatment treatment) { saved = treatment; return 9L; }
        @Override public boolean update(Treatment treatment) { saved = treatment; return true; }
        @Override public boolean updateActive(long id, boolean active) { return true; }
    }
}

package com.sunrisedental.dao;

import com.sunrisedental.model.Bill;
import com.sunrisedental.model.BillPayment;
import com.sunrisedental.model.BillingSource;
import com.sunrisedental.model.BillDetails;
import com.sunrisedental.model.BillableAppointment;

import java.util.List;
import java.util.Optional;

public interface BillDao {

    Optional<BillingSource> findBillingSourceByAppointmentNumber(
            String appointmentNumber
    );

    boolean existsByAppointmentId(long appointmentId);

    long create(Bill bill);

    Optional<Bill> findByBillNumber(String billNumber);

    Optional<Bill> findByAppointmentNumber(
            String appointmentNumber
    );

    List<Bill> findAll();

    void recordPayment(BillPayment payment);

    List<BillPayment> findPaymentsByBillId(long billId);

    Optional<BillDetails> findDetailsByBillNumber(
            String billNumber
    );

    List<BillDetails> findAllDetails();

    List<BillableAppointment>
    findCompletedUnbilledAppointments();

}
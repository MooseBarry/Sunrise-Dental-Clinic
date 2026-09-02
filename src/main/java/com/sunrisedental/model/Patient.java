package com.sunrisedental.model;

import java.time.LocalDate;

public record Patient(
        long patientId,
        String patientNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String nicNumber,
        String contactNumber,
        String email,
        String address,
        String medicalNotes
) {
}
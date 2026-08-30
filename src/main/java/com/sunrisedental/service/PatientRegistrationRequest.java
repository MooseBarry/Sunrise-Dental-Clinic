package com.sunrisedental.service;

import java.time.LocalDate;

public record PatientRegistrationRequest(
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
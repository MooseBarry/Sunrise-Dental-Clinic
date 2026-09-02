package com.sunrisedental.service;

import com.sunrisedental.model.Role;
import java.math.BigDecimal;

public record StaffRegistrationRequest(
        String username,
        String password,
        String fullName,
        String email,
        String contactNumber,
        Role role,
        String registrationNumber,
        String specialization,
        BigDecimal consultationFee
) {
}

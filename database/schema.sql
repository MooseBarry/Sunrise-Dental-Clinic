CREATE DATABASE IF NOT EXISTS sunrise_dental
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sunrise_dental;

CREATE TABLE IF NOT EXISTS roles (
                                     role_id INT PRIMARY KEY AUTO_INCREMENT,
                                     role_name VARCHAR(30) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users (
                                     user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE,
    contact_number VARCHAR(20),
    role_id INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
    );

CREATE TABLE IF NOT EXISTS patients (
                                        patient_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        patient_number VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(60) NOT NULL,
    last_name VARCHAR(60) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    nic_number VARCHAR(20) UNIQUE,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(120),
    address VARCHAR(255),
    medical_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS dentists (
                                        dentist_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        user_id BIGINT NOT NULL UNIQUE,
                                        registration_number VARCHAR(50) NOT NULL UNIQUE,
    specialization VARCHAR(100),
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_dentists_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    );

CREATE TABLE IF NOT EXISTS treatments (
                                          treatment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          treatment_code VARCHAR(20) NOT NULL UNIQUE,
    treatment_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    standard_fee DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS appointments (
                                            appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            appointment_number VARCHAR(25) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    dentist_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30,
    reason VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_appointment_status
    CHECK (
              status IN (
              'SCHEDULED',
              'COMPLETED',
              'CANCELLED',
              'NO_SHOW'
                        )
    ),

    CONSTRAINT chk_appointment_duration
    CHECK (duration_minutes > 0),

    CONSTRAINT uq_dentist_start_time
    UNIQUE (dentist_id, appointment_date, start_time),

    CONSTRAINT fk_appointments_patient
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),

    CONSTRAINT fk_appointments_dentist
    FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id),

    CONSTRAINT fk_appointments_creator
    FOREIGN KEY (created_by) REFERENCES users(user_id)
    );

CREATE TABLE IF NOT EXISTS appointment_treatments (
                                                      appointment_id BIGINT NOT NULL,
                                                      treatment_id BIGINT NOT NULL,
                                                      quantity INT NOT NULL DEFAULT 1,
                                                      charged_fee DECIMAL(10,2) NOT NULL,

    PRIMARY KEY (appointment_id, treatment_id),

    CONSTRAINT chk_treatment_quantity
    CHECK (quantity > 0),

    CONSTRAINT fk_appointment_treatments_appointment
    FOREIGN KEY (appointment_id)
    REFERENCES appointments(appointment_id)
    ON DELETE CASCADE,

    CONSTRAINT fk_appointment_treatments_treatment
    FOREIGN KEY (treatment_id)
    REFERENCES treatments(treatment_id)
    );

CREATE TABLE IF NOT EXISTS bills (
                                     bill_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     bill_number VARCHAR(25) NOT NULL UNIQUE,
    appointment_id BIGINT NOT NULL UNIQUE,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    treatment_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_status
    CHECK (payment_status IN ('UNPAID', 'PAID', 'PARTIALLY_PAID')),

    CONSTRAINT fk_bills_appointment
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)
    );
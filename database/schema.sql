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

    INDEX idx_appointments_dentist_slot (
        dentist_id,
        appointment_date,
        start_time
    ),

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
                                     consultation_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                                     treatment_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                                     discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                                     total_amount DECIMAL(10, 2) NOT NULL,
                                     amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                                     payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
                                     issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     issued_by BIGINT NULL,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,

                                     INDEX idx_bills_payment_status (payment_status),

                                     CONSTRAINT chk_payment_status
                                         CHECK (
                                             payment_status IN (
                                                                'UNPAID',
                                                                'PARTIALLY_PAID',
                                                                'PAID'
                                                 )
                                             ),

                                     CONSTRAINT chk_bill_amounts
                                         CHECK (
                                             consultation_fee >= 0.00
                                                 AND treatment_total >= 0.00
                                                 AND discount_amount >= 0.00
                                                 AND total_amount >= 0.00
                                                 AND discount_amount
                                                 <= consultation_fee + treatment_total
                                                 AND amount_paid >= 0.00
                                                 AND amount_paid <= total_amount
                                             ),

                                     CONSTRAINT chk_bill_payment_progress
                                         CHECK (
                                             (
                                                 payment_status = 'UNPAID'
                                                     AND amount_paid = 0.00
                                                 )
                                                 OR
                                             (
                                                 payment_status = 'PARTIALLY_PAID'
                                                     AND amount_paid > 0.00
                                                     AND amount_paid < total_amount
                                                 )
                                                 OR
                                             (
                                                 payment_status = 'PAID'
                                                     AND amount_paid = total_amount
                                                 )
                                             ),

                                     CONSTRAINT fk_bills_appointment
                                         FOREIGN KEY (appointment_id)
                                             REFERENCES appointments(appointment_id),

                                     CONSTRAINT fk_bills_issued_by
                                         FOREIGN KEY (issued_by)
                                             REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS bill_payments (
                                             payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             receipt_number VARCHAR(30) NOT NULL UNIQUE,
                                             bill_id BIGINT NOT NULL,
                                             amount DECIMAL(10, 2) NOT NULL,
                                             payment_method VARCHAR(20) NOT NULL,
                                             received_by BIGINT NOT NULL,
                                             paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             CONSTRAINT chk_bill_payment_amount
                                                 CHECK (amount > 0.00),

                                             CONSTRAINT chk_bill_payment_method
                                                 CHECK (
                                                     payment_method IN (
                                                                        'CASH',
                                                                        'CARD',
                                                                        'BANK_TRANSFER'
                                                         )
                                                     ),

                                             CONSTRAINT fk_bill_payments_bill
                                                 FOREIGN KEY (bill_id)
                                                     REFERENCES bills(bill_id),

                                             CONSTRAINT fk_bill_payments_receiver
                                                 FOREIGN KEY (received_by)
                                                     REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS staff_notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_user_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_type VARCHAR(30),
    reference_value VARCHAR(50),
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_notifications_recipient (
        recipient_user_id,
        read_at,
        created_at
    ),

    CONSTRAINT chk_notification_type
    CHECK (
        notification_type IN (
            'APPOINTMENT',
            'BILLING',
            'PAYMENT',
            'SYSTEM'
        )
    ),

    CONSTRAINT fk_notifications_recipient
    FOREIGN KEY (recipient_user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id BIGINT NULL,
    action_name VARCHAR(80) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_reference VARCHAR(60),
    details VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_entity (entity_type, entity_reference),

    CONSTRAINT fk_audit_actor
    FOREIGN KEY (actor_user_id)
    REFERENCES users(user_id)
    ON DELETE SET NULL
);

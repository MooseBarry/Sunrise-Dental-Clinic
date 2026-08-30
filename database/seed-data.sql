USE sunrise_dental;

-- Staff roles used for authorization
INSERT IGNORE INTO roles (role_name)
VALUES
    ('ADMIN'),
    ('RECEPTIONIST'),
    ('DENTIST');

-- Initial treatment catalogue
INSERT IGNORE INTO treatments (
    treatment_code,
    treatment_name,
    description,
    standard_fee,
    active
)
VALUES
    (
        'TRT-001',
        'Dental Examination',
        'General dental examination and consultation',
        2500.00,
        TRUE
    ),
    (
        'TRT-002',
        'Dental Cleaning',
        'Routine scaling and dental cleaning',
        5000.00,
        TRUE
    ),
    (
        'TRT-003',
        'Dental Filling',
        'Standard tooth filling treatment',
        7500.00,
        TRUE
    ),
    (
        'TRT-004',
        'Tooth Extraction',
        'Standard tooth extraction procedure',
        10000.00,
        TRUE
    ),
    (
        'TRT-005',
        'Root Canal Treatment',
        'Root canal treatment procedure',
        25000.00,
        TRUE
    ),
    (
        'TRT-006',
        'Dental Crown',
        'Standard dental crown procedure',
        35000.00,
        TRUE
    );

-- Initial administrator account
INSERT IGNORE INTO users (
    username,
    password_hash,
    full_name,
    email,
    contact_number,
    role_id,
    active
)
SELECT
    'admin',
    '$2a$12$tJvjYCulQTgRncJ1TQF1zezPthRW1.LXxj0TUO8eloYZN5QaWHjlu',
    'System Administrator',
    'admin@sunrisedental.lk',
    '0770000000',
    role_id,
    TRUE
FROM roles
WHERE role_name = 'ADMIN';
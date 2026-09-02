USE sunrise_dental;

INSERT IGNORE INTO roles (role_name)
VALUES ('CASHIER');

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
            'APPOINTMENT', 'BILLING', 'PAYMENT', 'SYSTEM'
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

-- Optional demo cashier account. It copies the existing administrator
-- BCrypt hash so the current demo password works after this migration.
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
    'cashier',
    administrator.password_hash,
    'Clinic Cashier',
    'cashier@sunrisedental.lk',
    '0770000002',
    cashier_role.role_id,
    TRUE
FROM users administrator
INNER JOIN roles cashier_role
    ON cashier_role.role_name = 'CASHIER'
WHERE administrator.username = 'admin';

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
    'reception',
    administrator.password_hash,
    'Clinic Receptionist',
    'reception@sunrisedental.lk',
    '0770000001',
    receptionist_role.role_id,
    TRUE
FROM users administrator
INNER JOIN roles receptionist_role
    ON receptionist_role.role_name = 'RECEPTIONIST'
WHERE administrator.username = 'admin';

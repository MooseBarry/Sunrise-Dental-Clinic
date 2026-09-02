USE sunrise_dental;

ALTER TABLE bills
    ADD COLUMN amount_paid DECIMAL(10, 2)
                                NOT NULL DEFAULT 0.00
        AFTER total_amount,

    ADD COLUMN issued_by BIGINT NULL
        AFTER issued_at,

    ADD COLUMN updated_at TIMESTAMP
                                NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        AFTER issued_by,

    ADD CONSTRAINT chk_bill_amounts
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

    ADD CONSTRAINT chk_bill_payment_progress
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

    ADD CONSTRAINT fk_bills_issued_by
        FOREIGN KEY (issued_by)
            REFERENCES users(user_id);

CREATE INDEX idx_bills_payment_status
    ON bills (payment_status);

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
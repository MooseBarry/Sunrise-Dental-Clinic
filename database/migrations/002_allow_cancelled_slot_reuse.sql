USE sunrise_dental;

-- Create the replacement index first so the dentist
-- foreign key remains supported
CREATE INDEX idx_appointments_dentist_slot
    ON appointments (
                     dentist_id,
                     appointment_date,
                     start_time
        );

-- Remove the old exact-start uniqueness restriction
ALTER TABLE appointments
    DROP INDEX uq_dentist_start_time;
ALTER TABLE booking
    ADD COLUMN owner_id BIGINT;

ALTER TABLE booking
    ADD CONSTRAINT fk_booking_user
        FOREIGN KEY (owner_id)
            REFERENCES user_entities(id);
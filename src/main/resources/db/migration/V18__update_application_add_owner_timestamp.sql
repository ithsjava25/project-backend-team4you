ALTER TABLE application_entity
    ADD COLUMN status VARCHAR(50);

ALTER TABLE application_entity
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE application_entity
    ADD COLUMN owner_user_id BIGINT;
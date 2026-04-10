ALTER TABLE user_credentials RENAME COLUMN id TO credential_id;
ALTER TABLE user_credentials RENAME COLUMN user_entity_id TO user_entity_user_id;

ALTER TABLE user_entities
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);

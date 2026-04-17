
ALTER TABLE IF EXISTS app_user RENAME TO user_entities;


ALTER TABLE user_entities
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS role VARCHAR(20);


UPDATE user_entities
SET email = name
WHERE email IS NULL AND name IS NOT NULL;


DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint
            WHERE conname = 'uk_user_entities_email'
        ) THEN
            ALTER TABLE user_entities
                ADD CONSTRAINT uk_user_entities_email UNIQUE (email);
        END IF;
    END $$;
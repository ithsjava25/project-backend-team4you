ALTER TABLE app_user RENAME TO user_entities;

ALTER TABLE user_entities DROP COLUMN IF EXISTS name;
ALTER TABLE user_entities RENAME COLUMN email TO name;

ALTER TABLE user_entities DROP COLUMN IF EXISTS display_name;
ALTER TABLE user_entities RENAME COLUMN first_name TO display_name;

ALTER TABLE user_credentials RENAME COLUMN user_entity_user_id TO user_entity_id;
ALTER TABLE user_credentials RENAME COLUMN credential_id TO id;

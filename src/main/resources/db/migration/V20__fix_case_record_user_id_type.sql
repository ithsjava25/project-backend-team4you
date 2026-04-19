ALTER TABLE case_record
    ALTER COLUMN assigned_user_id TYPE BIGINT USING assigned_user_id::bigint;

ALTER TABLE case_record
    ALTER COLUMN owner_user_id TYPE BIGINT USING owner_user_id::bigint;
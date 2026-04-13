ALTER TABLE booking
    ALTER COLUMN id TYPE BIGINT
    USING id::bigint;
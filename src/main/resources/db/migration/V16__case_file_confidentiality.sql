ALTER TABLE case_file
    ADD confidentiality_level VARCHAR(50);

UPDATE case_file
SET confidentiality_level = 'OPEN'
WHERE confidentiality_level IS NULL;

ALTER TABLE case_file
    ALTER COLUMN confidentiality_level SET NOT NULL;
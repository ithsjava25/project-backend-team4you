ALTER TABLE case_file
    ADD document_number INTEGER;

ALTER TABLE case_file
    ADD document_reference VARCHAR(255);

WITH numbered_files AS (
    SELECT
        cf.id,
        cf.case_record_id,
        cr.case_number,
        ROW_NUMBER() OVER (
            PARTITION BY cf.case_record_id
            ORDER BY cf.uploaded_at ASC, cf.id ASC
            ) AS new_document_number
    FROM case_file cf
             JOIN case_record cr ON cr.id = cf.case_record_id
)
UPDATE case_file cf
SET
    document_number = nf.new_document_number,
    document_reference = nf.case_number || '-' || nf.new_document_number
FROM numbered_files nf
WHERE cf.id = nf.id;

ALTER TABLE case_file
    ALTER COLUMN document_number SET NOT NULL;

ALTER TABLE case_file
    ALTER COLUMN document_reference SET NOT NULL;

ALTER TABLE case_file
    ADD CONSTRAINT uk_case_file_case_record_document_number
        UNIQUE (case_record_id, document_number);

ALTER TABLE case_file
    ADD CONSTRAINT uk_case_file_document_reference
        UNIQUE (document_reference);
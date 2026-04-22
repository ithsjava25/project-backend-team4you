ALTER TABLE case_file
    ALTER COLUMN document_number SET NOT NULL;

ALTER TABLE case_file
    ALTER COLUMN document_reference SET NOT NULL;

ALTER TABLE case_record
    ALTER COLUMN assigned_user_id DROP NOT NULL;
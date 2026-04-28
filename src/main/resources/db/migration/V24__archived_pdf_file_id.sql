ALTER TABLE protocol
    ADD COLUMN archived_pdf_file_id BIGINT;

ALTER TABLE protocol
    ADD CONSTRAINT fk_protocol_archived_pdf_file
        FOREIGN KEY (archived_pdf_file_id)
            REFERENCES case_file(id);

ALTER TABLE protocol
    ADD CONSTRAINT uk_protocol_archived_pdf_file
        UNIQUE (archived_pdf_file_id);
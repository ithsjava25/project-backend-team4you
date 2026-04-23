CREATE TABLE case_file_access (
                                  id BIGSERIAL PRIMARY KEY,
                                  case_record_id BIGINT NOT NULL REFERENCES case_record(id),
                                  user_id VARCHAR(255) NOT NULL REFERENCES user_entities(id),
                                  can_view_confidential_files BOOLEAN NOT NULL,
                                  CONSTRAINT uk_case_file_access_case_user UNIQUE (case_record_id, user_id)
);
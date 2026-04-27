CREATE TABLE meeting (
                         id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                         registry_id BIGINT NOT NULL,
                         title VARCHAR(200) NOT NULL,
                         starts_at TIMESTAMP NOT NULL,
                         ends_at TIMESTAMP,
                         location VARCHAR(200),
                         status VARCHAR(30) NOT NULL,
                         notes VARCHAR(2000),
                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT fk_meeting_registry
                             FOREIGN KEY (registry_id) REFERENCES registry(id)
);

CREATE TABLE meeting_agenda_item (
                                     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                     meeting_id BIGINT NOT NULL,
                                     case_record_id BIGINT NOT NULL,
                                     agenda_order INTEGER NOT NULL,
                                     agenda_note VARCHAR(1000),

                                     CONSTRAINT fk_meeting_agenda_item_meeting
                                         FOREIGN KEY (meeting_id) REFERENCES meeting(id) ON DELETE CASCADE,

                                     CONSTRAINT fk_meeting_agenda_item_case_record
                                         FOREIGN KEY (case_record_id) REFERENCES case_record(id),

                                     CONSTRAINT uk_meeting_agenda_item_meeting_case
                                         UNIQUE (meeting_id, case_record_id),

                                     CONSTRAINT uk_meeting_agenda_item_meeting_order
                                         UNIQUE (meeting_id, agenda_order)
);

CREATE TABLE meeting_agenda_document (
                                         id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                         agenda_item_id BIGINT NOT NULL,
                                         case_file_id BIGINT NOT NULL,

                                         CONSTRAINT fk_meeting_agenda_document_agenda_item
                                             FOREIGN KEY (agenda_item_id) REFERENCES meeting_agenda_item(id) ON DELETE CASCADE,

                                         CONSTRAINT fk_meeting_agenda_document_case_file
                                             FOREIGN KEY (case_file_id) REFERENCES case_file(id),

                                         CONSTRAINT uk_meeting_agenda_document_item_file
                                             UNIQUE (agenda_item_id, case_file_id)
);
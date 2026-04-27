CREATE TABLE registry_access (

                                 id BIGSERIAL PRIMARY KEY,

                                 registry_id BIGINT NOT NULL,
                                 user_id CHARACTER VARYING NOT NULL,

                                 can_create_cases BOOLEAN NOT NULL DEFAULT FALSE,

                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_registry_access_registry
                                     FOREIGN KEY (registry_id)
                                         REFERENCES registry(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_registry_access_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES user_entities(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT uq_registry_access_unique
                                     UNIQUE (registry_id, user_id)

);
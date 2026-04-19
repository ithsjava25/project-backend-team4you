ALTER TABLE application_entity
    ADD column if not exists title varchar(255),
    ADD column if not exists description varchar(255);


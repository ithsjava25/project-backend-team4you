alter table protocol_paragraph
    drop constraint fk_protocol_paragraph_protocol;

alter table protocol_paragraph
    add constraint fk_protocol_paragraph_protocol
        foreign key (protocol_id)
            references protocol(id)
            on delete cascade;
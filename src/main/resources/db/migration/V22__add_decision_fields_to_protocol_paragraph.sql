alter table protocol_paragraph
    add column decision_type varchar(30),
    add column decision_text varchar(2000);
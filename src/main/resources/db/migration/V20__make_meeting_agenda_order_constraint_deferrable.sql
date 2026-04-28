ALTER TABLE meeting_agenda_item
    DROP CONSTRAINT uk_meeting_agenda_item_meeting_order;

ALTER TABLE meeting_agenda_item
    ADD CONSTRAINT uk_meeting_agenda_item_meeting_order
        UNIQUE (meeting_id, agenda_order)
            DEFERRABLE INITIALLY IMMEDIATE;
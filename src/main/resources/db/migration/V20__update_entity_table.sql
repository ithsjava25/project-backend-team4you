    ALTER TABLE audit
DROP COLUMN http_method,
ADD COLUMN entity_type VARCHAR (255),
ADD COLUMN entity_id BIGINT

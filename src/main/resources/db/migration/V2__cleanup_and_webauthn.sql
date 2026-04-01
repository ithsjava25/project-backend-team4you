CREATE TABLE app_user (
                          id VARCHAR(255) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL UNIQUE,
                          display_name VARCHAR(255),
                          password_hash VARCHAR(255), -- NULL permitido para Passkeys
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_credentials (
                                  credential_id VARCHAR(512) PRIMARY KEY,
                                  user_entity_user_id VARCHAR(255) NOT NULL,
                                  public_key BYTEA NOT NULL,
                                  attestation_object BYTEA,
                                  attestation_client_data_json BYTEA,
                                  signature_count BIGINT DEFAULT 0,
                                  uv_initialized BOOLEAN DEFAULT FALSE,
                                  backup_eligible BOOLEAN DEFAULT FALSE,
                                  backup_state BOOLEAN DEFAULT FALSE,
                                  created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                  last_used TIMESTAMP WITH TIME ZONE,
                                  label VARCHAR(255),
                                  public_key_credential_type VARCHAR(50) DEFAULT 'public-key',
                                  authenticator_transports VARCHAR(255),
                                  CONSTRAINT fk_user_entity FOREIGN KEY (user_entity_user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

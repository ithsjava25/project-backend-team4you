CREATE TABLE booking (
                         id VARCHAR(36) PRIMARY KEY,
                         name VARCHAR(255),
                         email VARCHAR(255),
                         phone VARCHAR(50),

                         booking_time TIMESTAMP WITH TIME ZONE,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                         status VARCHAR(50)
);
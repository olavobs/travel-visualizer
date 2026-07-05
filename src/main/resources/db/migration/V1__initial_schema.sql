CREATE TABLE users (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE travel_routes (
    id           BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    origin       VARCHAR(10) NOT NULL,
    destination  VARCHAR(10) NOT NULL,
    travel_date  DATE        NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'WATCHING',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_route UNIQUE (user_id, origin, destination, travel_date),
    CONSTRAINT fk_route_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_travel_routes_user_id ON travel_routes (user_id);

CREATE TABLE travel_segments (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    route_id       BIGINT       NOT NULL,
    transport_type VARCHAR(20)  NOT NULL,
    label          VARCHAR(100) NULL,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_segment_route FOREIGN KEY (route_id) REFERENCES travel_routes (id) ON DELETE CASCADE
);

CREATE INDEX idx_segments_route_id ON travel_segments (route_id);

CREATE TABLE price_records (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    segment_id  BIGINT         NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL DEFAULT 'BRL',
    recorded_at DATETIME       NOT NULL,
    purchased   BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_segment FOREIGN KEY (segment_id) REFERENCES travel_segments (id) ON DELETE CASCADE
);

CREATE INDEX idx_price_records_segment_id ON price_records (segment_id);
CREATE INDEX idx_price_records_recorded_at ON price_records (segment_id, recorded_at DESC);

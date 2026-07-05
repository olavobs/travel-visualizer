CREATE TABLE users (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE flight_routes (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    origin      VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    flight_date DATE        NOT NULL,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_route UNIQUE (user_id, origin, destination, flight_date),
    CONSTRAINT fk_route_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_flight_routes_user_id ON flight_routes (user_id);

CREATE TABLE price_records (
    id            BIGINT         AUTO_INCREMENT PRIMARY KEY,
    route_id      BIGINT         NOT NULL,
    price         DECIMAL(10, 2) NOT NULL,
    currency      VARCHAR(3)     NOT NULL DEFAULT 'BRL',
    recorded_date DATE           NOT NULL,
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_route FOREIGN KEY (route_id) REFERENCES flight_routes (id) ON DELETE CASCADE
);

CREATE INDEX idx_price_records_route_id ON price_records (route_id);
CREATE INDEX idx_price_records_recorded_date ON price_records (route_id, recorded_date DESC);

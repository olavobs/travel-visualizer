-- Rename flight_routes to travel_routes and flight_date to travel_date
RENAME TABLE flight_routes TO travel_routes;
ALTER TABLE travel_routes CHANGE flight_date travel_date DATE NOT NULL;

-- Segments table: one row per transport mode within a route
CREATE TABLE travel_segments (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    route_id       BIGINT       NOT NULL,
    transport_type VARCHAR(20)  NOT NULL,
    label          VARCHAR(100) NULL,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_segment_route FOREIGN KEY (route_id) REFERENCES travel_routes(id) ON DELETE CASCADE
);
CREATE INDEX idx_segments_route_id ON travel_segments (route_id);

-- Add segment_id to price_records
ALTER TABLE price_records ADD COLUMN segment_id BIGINT NULL;

-- Migrate existing data: create one FLIGHT segment per existing route
INSERT INTO travel_segments (route_id, transport_type)
SELECT id, 'FLIGHT' FROM travel_routes;

-- Point existing price records to their route's default segment
UPDATE price_records pr
INNER JOIN travel_segments ts ON ts.route_id = pr.route_id
SET pr.segment_id = ts.id;

-- Make segment_id NOT NULL and add FK
ALTER TABLE price_records
    MODIFY COLUMN segment_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_price_segment FOREIGN KEY (segment_id) REFERENCES travel_segments(id) ON DELETE CASCADE;

-- Drop old route_id FK and column from price_records
ALTER TABLE price_records DROP FOREIGN KEY fk_price_route;
ALTER TABLE price_records DROP COLUMN route_id;

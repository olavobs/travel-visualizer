-- Replace boolean booked column with a status enum column
ALTER TABLE travel_routes ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'WATCHING';

UPDATE travel_routes SET status = 'BOOKED' WHERE booked = TRUE;

ALTER TABLE travel_routes DROP COLUMN booked;

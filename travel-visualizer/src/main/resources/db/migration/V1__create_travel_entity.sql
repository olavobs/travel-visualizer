CREATE SEQUENCE IF NOT EXISTS node_sequence START 1;

CREATE TABLE IF NOT EXISTS node
(
    id                     BIGINT DEFAULT nextval('node_sequence') PRIMARY KEY,
    current_city           VARCHAR(255) NOT NULL,
    price                  NUMERIC(19, 2),
    start_moment           TIMESTAMP WITH TIME ZONE,
    end_moment             TIMESTAMP WITH TIME ZONE,
    currency               VARCHAR(3),
    transport_company_name VARCHAR(255),
    departure_place        VARCHAR(255),
    arrival_place          VARCHAR(255),
    transport_type         VARCHAR(20),
    previous_node          BIGINT REFERENCES node (id)
);

ALTER TABLE node
    ADD CONSTRAINT uk_current_city_per_previous_node UNIQUE (current_city, previous_node);
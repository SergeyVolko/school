CREATE TABLE cars (
    car_id SERIAL PRIMARY KEY,
    model VARCHAR(50) NOT NULL,
    cost NUMERIC(10, 2) DEFAULT 0
);

CREATE TABLE drivers(
    driver_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INTEGER CHECK (age > 0),
    is_drive BOOLEAN,
    car_id INTEGER NOT NULL REFERENCES cars(car_id)
);
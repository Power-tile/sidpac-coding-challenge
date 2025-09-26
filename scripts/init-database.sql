-- Flight Search Engine Database Initialization Script
-- This script creates the database schema and loads initial data
-- Run this script to initialize a fresh database

-- Create the data directory if it doesn't exist
-- mkdir -p data

-- Initialize the database
-- sqlite3 data/flight_search.db < scripts/init-database.sql

-- Drop existing tables if they exist (for clean initialization)
DROP TABLE IF EXISTS fare_restrictions;
DROP TABLE IF EXISTS fares;
DROP TABLE IF EXISTS flight_airlines;
DROP TABLE IF EXISTS flights;
DROP TABLE IF EXISTS airports;
DROP TABLE IF EXISTS airlines;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS users;

-- Create tables (from V1__Create_initial_schema.sql)
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER', 'GUEST')),
    assigned_airline_code TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

CREATE TABLE airlines (
    id TEXT PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    country TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_airlines_code ON airlines(code);

CREATE TABLE airports (
    id TEXT PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    city TEXT NOT NULL,
    country TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_airports_code ON airports(code);
CREATE INDEX idx_airports_city ON airports(city);

CREATE TABLE flights (
    id TEXT PRIMARY KEY,
    flight_number TEXT NOT NULL,
    source_airport_id TEXT NOT NULL,
    destination_airport_id TEXT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_airport_id) REFERENCES airports(id),
    FOREIGN KEY (destination_airport_id) REFERENCES airports(id)
);

CREATE INDEX idx_flights_source_destination ON flights(source_airport_id, destination_airport_id);
CREATE INDEX idx_flights_departure_time ON flights(departure_time);
CREATE INDEX idx_flights_arrival_time ON flights(arrival_time);

CREATE TABLE flight_airlines (
    id TEXT PRIMARY KEY,
    flight_id TEXT NOT NULL,
    airline_id TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    FOREIGN KEY (airline_id) REFERENCES airlines(id) ON DELETE CASCADE,
    UNIQUE (flight_id, airline_id)
);

CREATE INDEX idx_flight_airlines_flight_id ON flight_airlines(flight_id);
CREATE INDEX idx_flight_airlines_airline_id ON flight_airlines(airline_id);

CREATE TABLE fares (
    id TEXT PRIMARY KEY,
    airline_id TEXT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    fare_name TEXT NOT NULL,
    description TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airline_id) REFERENCES airlines(id) ON DELETE CASCADE
);

CREATE INDEX idx_fares_airline_id ON fares(airline_id);
CREATE INDEX idx_fares_base_price ON fares(base_price);

CREATE TABLE fare_restrictions (
    id TEXT PRIMARY KEY,
    fare_id TEXT NOT NULL,
    restriction_type TEXT NOT NULL CHECK (restriction_type IN ('ENDPOINT', 'DEPARTURE_TIME', 'MULTI_LEG')),
    restriction_value TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fare_id) REFERENCES fares(id) ON DELETE CASCADE
);

CREATE INDEX idx_fare_restrictions_fare_id ON fare_restrictions(fare_id);
CREATE INDEX idx_fare_restrictions_type ON fare_restrictions(restriction_type);

CREATE TABLE user_sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL,
    refresh_token_hash TEXT NOT NULL,
    expires_at DATETIME NOT NULL,
    refresh_expires_at DATETIME NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);

-- Load initial data (from data.sql)
-- Insert sample airports
INSERT INTO airports (id, code, name, city, country, is_active) VALUES
-- US Airports
('airport-bos-001', 'BOS', 'Logan International Airport', 'Boston', 'USA', 1),
('airport-lax-001', 'LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA', 1),
('airport-jfk-001', 'JFK', 'John F. Kennedy International Airport', 'New York', 'USA', 1),
('airport-lga-001', 'LGA', 'LaGuardia Airport', 'New York', 'USA', 1),
('airport-ord-001', 'ORD', 'O''Hare International Airport', 'Chicago', 'USA', 1),
('airport-dfw-001', 'DFW', 'Dallas/Fort Worth International Airport', 'Dallas', 'USA', 1),
('airport-atl-001', 'ATL', 'Hartsfield-Jackson Atlanta International Airport', 'Atlanta', 'USA', 1),
('airport-mia-001', 'MIA', 'Miami International Airport', 'Miami', 'USA', 1),
('airport-sea-001', 'SEA', 'Seattle-Tacoma International Airport', 'Seattle', 'USA', 1),
('airport-den-001', 'DEN', 'Denver International Airport', 'Denver', 'USA', 1),
('airport-sfo-001', 'SFO', 'San Francisco International Airport', 'San Francisco', 'USA', 1),
('airport-las-001', 'LAS', 'McCarran International Airport', 'Las Vegas', 'USA', 1),

-- International Airports
('airport-lhr-001', 'LHR', 'London Heathrow Airport', 'London', 'UK', 1),
('airport-cdg-001', 'CDG', 'Charles de Gaulle Airport', 'Paris', 'France', 1),
('airport-fra-001', 'FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany', 1),
('airport-nrt-001', 'NRT', 'Narita International Airport', 'Tokyo', 'Japan', 1),
('airport-hnd-001', 'HND', 'Haneda Airport', 'Tokyo', 'Japan', 1),
('airport-syd-001', 'SYD', 'Kingsford Smith Airport', 'Sydney', 'Australia', 1),
('airport-yyz-001', 'YYZ', 'Toronto Pearson International Airport', 'Toronto', 'Canada', 1),
('airport-yvr-001', 'YVR', 'Vancouver International Airport', 'Vancouver', 'Canada', 1);

-- Insert sample airlines
INSERT INTO airlines (id, code, name, country, is_active) VALUES
-- US Airlines
('airline-aa-001', 'AA', 'American Airlines', 'USA', 1),
('airline-dl-001', 'DL', 'Delta Air Lines', 'USA', 1),
('airline-ua-001', 'UA', 'United Airlines', 'USA', 1),
('airline-b6-001', 'B6', 'JetBlue Airways', 'USA', 1),
('airline-wn-001', 'WN', 'Southwest Airlines', 'USA', 1),
('airline-as-001', 'AS', 'Alaska Airlines', 'USA', 1),
('airline-f9-001', 'F9', 'Frontier Airlines', 'USA', 1),
('airline-nk-001', 'NK', 'Spirit Airlines', 'USA', 1),

-- International Airlines
('airline-ba-001', 'BA', 'British Airways', 'UK', 1),
('airline-af-001', 'AF', 'Air France', 'France', 1),
('airline-lh-001', 'LH', 'Lufthansa', 'Germany', 1),
('airline-jl-001', 'JL', 'Japan Airlines', 'Japan', 1),
('airline-nh-001', 'NH', 'All Nippon Airways', 'Japan', 1),
('airline-qf-001', 'QF', 'Qantas Airways', 'Australia', 1),
('airline-ac-001', 'AC', 'Air Canada', 'Canada', 1),
('airline-kl-001', 'KL', 'KLM Royal Dutch Airlines', 'Netherlands', 1);

-- Insert sample users
-- Password for all users is 'password123' (hashed with BCrypt)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, assigned_airline_code, is_active) VALUES
-- Super Admin
('user-admin-001', 'admin', 'admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Super', 'Admin', 'ADMIN', NULL, 1),

-- Airline Admins
('user-aa-admin-001', 'aa_admin', 'aa_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'American', 'Admin', 'ADMIN', 'AA', 1),
('user-dl-admin-001', 'dl_admin', 'dl_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Delta', 'Admin', 'ADMIN', 'DL', 1),
('user-ua-admin-001', 'ua_admin', 'ua_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'United', 'Admin', 'ADMIN', 'UA', 1),
('user-b6-admin-001', 'b6_admin', 'b6_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'JetBlue', 'Admin', 'ADMIN', 'B6', 1),

-- Regular Users
('user-regular-001', 'user1', 'user1@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'John', 'Doe', 'USER', NULL, 1),
('user-regular-002', 'user2', 'user2@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Jane', 'Smith', 'USER', NULL, 1),
('user-regular-003', 'user3', 'user3@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Bob', 'Johnson', 'USER', NULL, 1),

-- Guest Users
('user-guest-001', 'guest1', 'guest1@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Guest', 'User', 'GUEST', NULL, 1);

-- Insert sample flights (scheduled for March 20, 2024)
INSERT INTO flights (id, flight_number, source_airport_id, destination_airport_id, departure_time, arrival_time, is_active) VALUES
-- American Airlines flights
('flight-aa123-001', 'AA123', 'airport-bos-001', 'airport-lax-001', '2024-03-20 09:30:00', '2024-03-20 15:30:00', 1),
('flight-aa456-001', 'AA456', 'airport-lax-001', 'airport-bos-001', '2024-03-20 18:00:00', '2024-03-21 00:30:00', 1),
('flight-aa789-001', 'AA789', 'airport-jfk-001', 'airport-mia-001', '2024-03-20 07:00:00', '2024-03-20 10:30:00', 1),
('flight-aa101-001', 'AA101', 'airport-dfw-001', 'airport-lax-001', '2024-03-20 14:00:00', '2024-03-20 16:30:00', 1),

-- Delta flights
('flight-dl789-001', 'DL789', 'airport-jfk-001', 'airport-lax-001', '2024-03-20 10:00:00', '2024-03-20 16:30:00', 1),
('flight-dl234-001', 'DL234', 'airport-atl-001', 'airport-lax-001', '2024-03-20 11:30:00', '2024-03-20 14:45:00', 1),
('flight-dl567-001', 'DL567', 'airport-sea-001', 'airport-jfk-001', '2024-03-20 06:00:00', '2024-03-20 14:30:00', 1),

-- United flights
('flight-ua101-001', 'UA101', 'airport-bos-001', 'airport-ord-001', '2024-03-20 08:00:00', '2024-03-20 10:30:00', 1),
('flight-ua102-001', 'UA102', 'airport-ord-001', 'airport-lax-001', '2024-03-20 12:00:00', '2024-03-20 15:00:00', 1),
('flight-ua303-001', 'UA303', 'airport-sfo-001', 'airport-ord-001', '2024-03-20 13:00:00', '2024-03-20 20:30:00', 1),
('flight-ua404-001', 'UA404', 'airport-den-001', 'airport-jfk-001', '2024-03-20 15:30:00', '2024-03-20 22:00:00', 1),

-- JetBlue flights
('flight-b6-501-001', 'B6-501', 'airport-jfk-001', 'airport-lax-001', '2024-03-20 16:00:00', '2024-03-20 19:30:00', 1),
('flight-b6-502-001', 'B6-502', 'airport-bos-001', 'airport-las-001', '2024-03-20 17:30:00', '2024-03-20 21:00:00', 1),

-- Southwest flights
('flight-wn-601-001', 'WN-601', 'airport-las-001', 'airport-lax-001', '2024-03-20 09:00:00', '2024-03-20 10:15:00', 1),
('flight-wn-602-001', 'WN-602', 'airport-den-001', 'airport-las-001', '2024-03-20 12:30:00', '2024-03-20 13:45:00', 1),

-- International flights
('flight-ba-701-001', 'BA-701', 'airport-lhr-001', 'airport-jfk-001', '2024-03-20 10:30:00', '2024-03-20 13:30:00', 1),
('flight-af-801-001', 'AF-801', 'airport-cdg-001', 'airport-jfk-001', '2024-03-20 11:00:00', '2024-03-20 14:00:00', 1),
('flight-jl-901-001', 'JL-901', 'airport-nrt-001', 'airport-lax-001', '2024-03-20 14:30:00', '2024-03-20 09:30:00', 1);

-- Insert flight-airline relationships (codeshare)
INSERT INTO flight_airlines (id, flight_id, airline_id, is_active) VALUES
-- American Airlines flights
('fa-aa123-aa-001', 'flight-aa123-001', 'airline-aa-001', 1),
('fa-aa123-b6-001', 'flight-aa123-001', 'airline-b6-001', 1), -- Codeshare with JetBlue
('fa-aa456-aa-001', 'flight-aa456-001', 'airline-aa-001', 1),
('fa-aa789-aa-001', 'flight-aa789-001', 'airline-aa-001', 1),
('fa-aa101-aa-001', 'flight-aa101-001', 'airline-aa-001', 1),

-- Delta flights
('fa-dl789-dl-001', 'flight-dl789-001', 'airline-dl-001', 1),
('fa-dl234-dl-001', 'flight-dl234-001', 'airline-dl-001', 1),
('fa-dl567-dl-001', 'flight-dl567-001', 'airline-dl-001', 1),

-- United flights
('fa-ua101-ua-001', 'flight-ua101-001', 'airline-ua-001', 1),
('fa-ua102-ua-001', 'flight-ua102-001', 'airline-ua-001', 1),
('fa-ua303-ua-001', 'flight-ua303-001', 'airline-ua-001', 1),
('fa-ua404-ua-001', 'flight-ua404-001', 'airline-ua-001', 1),

-- JetBlue flights
('fa-b6-501-b6-001', 'flight-b6-501-001', 'airline-b6-001', 1),
('fa-b6-502-b6-001', 'flight-b6-502-001', 'airline-b6-001', 1),

-- Southwest flights
('fa-wn-601-wn-001', 'flight-wn-601-001', 'airline-wn-001', 1),
('fa-wn-602-wn-001', 'flight-wn-602-001', 'airline-wn-001', 1),

-- International flights
('fa-ba-701-ba-001', 'flight-ba-701-001', 'airline-ba-001', 1),
('fa-af-801-af-001', 'flight-af-801-001', 'airline-af-001', 1),
('fa-jl-901-jl-001', 'flight-jl-901-001', 'airline-jl-001', 1);

-- Insert sample fares
INSERT INTO fares (id, airline_id, base_price, fare_name, description, is_active) VALUES
-- American Airlines fares
('fare-aa-standard-001', 'airline-aa-001', 200.00, 'Standard Fare', 'Any flight has fare $200', 1),
('fare-aa-bos-special-001', 'airline-aa-001', 150.00, 'BOS Special', 'Any flight departing/arriving at BOS has fare $150', 1),
('fare-aa-early-bird-001', 'airline-aa-001', 175.00, 'Early Bird', 'Any flight leaving before 09:00 has fare $175', 1),
('fare-aa-multi-leg-001', 'airline-aa-001', 160.00, 'Multi-Leg Discount', 'Any flight as part of a 2-or-more leg trip has fare $160', 1),
('fare-aa-dfw-hub-001', 'airline-aa-001', 130.00, 'DFW Hub Special', 'Any flight departing/arriving at DFW has fare $130', 1),

-- Delta fares
('fare-dl-standard-001', 'airline-dl-001', 220.00, 'Standard Fare', 'Any flight has fare $220', 1),
('fare-dl-jfk-special-001', 'airline-dl-001', 180.00, 'JFK Special', 'Any flight departing/arriving at JFK has fare $180', 1),
('fare-dl-atl-hub-001', 'airline-dl-001', 140.00, 'ATL Hub Special', 'Any flight departing/arriving at ATL has fare $140', 1),
('fare-dl-west-coast-001', 'airline-dl-001', 200.00, 'West Coast Special', 'Any flight to/from LAX has fare $200', 1),

-- United fares
('fare-ua-standard-001', 'airline-ua-001', 190.00, 'Standard Fare', 'Any flight has fare $190', 1),
('fare-ua-ord-hub-001', 'airline-ua-001', 140.00, 'ORD Hub', 'Any flight departing/arriving at ORD has fare $140', 1),
('fare-ua-sfo-hub-001', 'airline-ua-001', 150.00, 'SFO Hub', 'Any flight departing/arriving at SFO has fare $150', 1),
('fare-ua-den-hub-001', 'airline-ua-001', 160.00, 'DEN Hub', 'Any flight departing/arriving at DEN has fare $160', 1),

-- JetBlue fares
('fare-b6-standard-001', 'airline-b6-001', 180.00, 'Standard Fare', 'Any flight has fare $180', 1),
('fare-b6-jfk-hub-001', 'airline-b6-001', 120.00, 'JFK Hub Special', 'Any flight departing/arriving at JFK has fare $120', 1),
('fare-b6-bos-hub-001', 'airline-b6-001', 130.00, 'BOS Hub Special', 'Any flight departing/arriving at BOS has fare $130', 1),

-- Southwest fares
('fare-wn-standard-001', 'airline-wn-001', 160.00, 'Standard Fare', 'Any flight has fare $160', 1),
('fare-wn-las-hub-001', 'airline-wn-001', 100.00, 'LAS Hub Special', 'Any flight departing/arriving at LAS has fare $100', 1),
('fare-wn-den-hub-001', 'airline-wn-001', 110.00, 'DEN Hub Special', 'Any flight departing/arriving at DEN has fare $110', 1),

-- International airline fares
('fare-ba-standard-001', 'airline-ba-001', 800.00, 'Standard Fare', 'Any international flight has fare $800', 1),
('fare-af-standard-001', 'airline-af-001', 750.00, 'Standard Fare', 'Any international flight has fare $750', 1),
('fare-jl-standard-001', 'airline-jl-001', 900.00, 'Standard Fare', 'Any international flight has fare $900', 1);

-- Insert fare restrictions
INSERT INTO fare_restrictions (id, fare_id, restriction_type, restriction_value, is_active) VALUES
-- American Airlines restrictions
('restriction-bos-special-001', 'fare-aa-bos-special-001', 'ENDPOINT', 'BOS', 1),
('restriction-early-bird-001', 'fare-aa-early-bird-001', 'DEPARTURE_TIME', '09:00', 1),
('restriction-multi-leg-001', 'fare-aa-multi-leg-001', 'MULTI_LEG', '2', 1),
('restriction-dfw-hub-001', 'fare-aa-dfw-hub-001', 'ENDPOINT', 'DFW', 1),

-- Delta restrictions
('restriction-jfk-special-001', 'fare-dl-jfk-special-001', 'ENDPOINT', 'JFK', 1),
('restriction-atl-hub-001', 'fare-dl-atl-hub-001', 'ENDPOINT', 'ATL', 1),
('restriction-west-coast-001', 'fare-dl-west-coast-001', 'ENDPOINT', 'LAX', 1),

-- United restrictions
('restriction-ord-hub-001', 'fare-ua-ord-hub-001', 'ENDPOINT', 'ORD', 1),
('restriction-sfo-hub-001', 'fare-ua-sfo-hub-001', 'ENDPOINT', 'SFO', 1),
('restriction-den-hub-001', 'fare-ua-den-hub-001', 'ENDPOINT', 'DEN', 1),

-- JetBlue restrictions
('restriction-jfk-hub-001', 'fare-b6-jfk-hub-001', 'ENDPOINT', 'JFK', 1),
('restriction-bos-hub-001', 'fare-b6-bos-hub-001', 'ENDPOINT', 'BOS', 1),

-- Southwest restrictions
('restriction-las-hub-001', 'fare-wn-las-hub-001', 'ENDPOINT', 'LAS', 1),
('restriction-den-hub-wn-001', 'fare-wn-den-hub-001', 'ENDPOINT', 'DEN', 1);

-- Display summary
SELECT 'Database initialization completed successfully!' as status;
SELECT 'Total airports: ' || COUNT(*) as airports FROM airports;
SELECT 'Total airlines: ' || COUNT(*) as airlines FROM airlines;
SELECT 'Total flights: ' || COUNT(*) as flights FROM flights;
SELECT 'Total users: ' || COUNT(*) as users FROM users;
SELECT 'Total fares: ' || COUNT(*) as fares FROM fares;

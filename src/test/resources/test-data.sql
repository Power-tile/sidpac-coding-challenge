-- H2-compatible test data for Flight Search Engine
-- This script only contains data insertion, assuming schema already exists

-- Insert sample airports
INSERT INTO airports (id, code, name, city, country, created_at, updated_at) VALUES
-- US Airports
('airport-bos-001', 'BOS', 'Logan International Airport', 'Boston', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-lax-001', 'LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-jfk-001', 'JFK', 'John F. Kennedy International Airport', 'New York', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-lga-001', 'LGA', 'LaGuardia Airport', 'New York', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-ord-001', 'ORD', 'O''Hare International Airport', 'Chicago', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-dfw-001', 'DFW', 'Dallas/Fort Worth International Airport', 'Dallas', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-atl-001', 'ATL', 'Hartsfield-Jackson Atlanta International Airport', 'Atlanta', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-mia-001', 'MIA', 'Miami International Airport', 'Miami', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-sea-001', 'SEA', 'Seattle-Tacoma International Airport', 'Seattle', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-den-001', 'DEN', 'Denver International Airport', 'Denver', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-sfo-001', 'SFO', 'San Francisco International Airport', 'San Francisco', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-las-001', 'LAS', 'McCarran International Airport', 'Las Vegas', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- International Airports
('airport-lhr-001', 'LHR', 'London Heathrow Airport', 'London', 'UK', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-cdg-001', 'CDG', 'Charles de Gaulle Airport', 'Paris', 'France', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-fra-001', 'FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-nrt-001', 'NRT', 'Narita International Airport', 'Tokyo', 'Japan', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-hnd-001', 'HND', 'Haneda Airport', 'Tokyo', 'Japan', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-syd-001', 'SYD', 'Kingsford Smith Airport', 'Sydney', 'Australia', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-yyz-001', 'YYZ', 'Toronto Pearson International Airport', 'Toronto', 'Canada', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airport-yvr-001', 'YVR', 'Vancouver International Airport', 'Vancouver', 'Canada', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert sample airlines
INSERT INTO airlines (id, code, name, country, created_at, updated_at) VALUES
-- US Airlines
('airline-aa-001', 'AA', 'American Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-dl-001', 'DL', 'Delta Air Lines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-ua-001', 'UA', 'United Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-b6-001', 'B6', 'JetBlue Airways', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-wn-001', 'WN', 'Southwest Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-as-001', 'AS', 'Alaska Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-f9-001', 'F9', 'Frontier Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-nk-001', 'NK', 'Spirit Airlines', 'USA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- International Airlines
('airline-ba-001', 'BA', 'British Airways', 'UK', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-af-001', 'AF', 'Air France', 'France', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-lh-001', 'LH', 'Lufthansa', 'Germany', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-jl-001', 'JL', 'Japan Airlines', 'Japan', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-nh-001', 'NH', 'All Nippon Airways', 'Japan', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-qf-001', 'QF', 'Qantas Airways', 'Australia', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-ac-001', 'AC', 'Air Canada', 'Canada', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('airline-kl-001', 'KL', 'KLM Royal Dutch Airlines', 'Netherlands', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert sample users
-- Password for all users is 'password123' (hashed with BCrypt)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, role, assigned_airline_code, created_at, updated_at) VALUES
-- Super Admin
('user-admin-001', 'admin', 'admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Super', 'Admin', 'ADMIN', NULL, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- Airline Admins
('user-aa-admin-001', 'aa_admin', 'aa_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'American', 'Admin', 'ADMIN', 'AA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user-dl-admin-001', 'dl_admin', 'dl_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Delta', 'Admin', 'ADMIN', 'DL', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user-ua-admin-001', 'ua_admin', 'ua_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'United', 'Admin', 'ADMIN', 'UA', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('user-b6-admin-001', 'b6_admin', 'b6_admin@flightsearch.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'JetBlue', 'Admin', 'ADMIN', 'B6', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert sample flights (scheduled for March 20, 2024)
INSERT INTO flights (id, flight_number, source_airport_id, destination_airport_id, departure_time, arrival_time, created_at, updated_at) VALUES
-- American Airlines flights
('flight-aa123-001', 'AA123', 'airport-bos-001', 'airport-lax-001', '2024-03-20 09:30:00', '2024-03-20 15:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-aa456-001', 'AA456', 'airport-lax-001', 'airport-bos-001', '2024-03-20 18:00:00', '2024-03-21 00:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-aa789-001', 'AA789', 'airport-jfk-001', 'airport-mia-001', '2024-03-20 07:00:00', '2024-03-20 10:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-aa101-001', 'AA101', 'airport-dfw-001', 'airport-lax-001', '2024-03-20 14:00:00', '2024-03-20 16:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- Delta flights
('flight-dl789-001', 'DL789', 'airport-jfk-001', 'airport-lax-001', '2024-03-20 10:00:00', '2024-03-20 16:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-dl234-001', 'DL234', 'airport-atl-001', 'airport-lax-001', '2024-03-20 11:30:00', '2024-03-20 14:45:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-dl567-001', 'DL567', 'airport-sea-001', 'airport-jfk-001', '2024-03-20 06:00:00', '2024-03-20 14:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- United flights
('flight-ua101-001', 'UA101', 'airport-bos-001', 'airport-ord-001', '2024-03-20 08:00:00', '2024-03-20 10:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-ua102-001', 'UA102', 'airport-ord-001', 'airport-lax-001', '2024-03-20 12:00:00', '2024-03-20 15:00:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-ua303-001', 'UA303', 'airport-sfo-001', 'airport-ord-001', '2024-03-20 13:00:00', '2024-03-20 20:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-ua404-001', 'UA404', 'airport-den-001', 'airport-jfk-001', '2024-03-20 15:30:00', '2024-03-20 22:00:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- JetBlue flights
('flight-b6-501-001', 'B6-501', 'airport-jfk-001', 'airport-lax-001', '2024-03-20 16:00:00', '2024-03-20 19:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-b6-502-001', 'B6-502', 'airport-bos-001', 'airport-las-001', '2024-03-20 17:30:00', '2024-03-20 21:00:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- Southwest flights
('flight-wn-601-001', 'WN-601', 'airport-las-001', 'airport-lax-001', '2024-03-20 09:00:00', '2024-03-20 10:15:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-wn-602-001', 'WN-602', 'airport-den-001', 'airport-las-001', '2024-03-20 12:30:00', '2024-03-20 13:45:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- International flights
('flight-ba-701-001', 'BA-701', 'airport-lhr-001', 'airport-jfk-001', '2024-03-20 10:30:00', '2024-03-20 13:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-af-801-001', 'AF-801', 'airport-cdg-001', 'airport-jfk-001', '2024-03-20 11:00:00', '2024-03-20 14:00:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('flight-jl-901-001', 'JL-901', 'airport-nrt-001', 'airport-lax-001', '2024-03-20 14:30:00', '2024-03-20 09:30:00', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert flight-airline relationships (codeshare)
INSERT INTO flight_airlines (id, flight_id, airline_id, created_at) VALUES
-- American Airlines flights
('fa-aa123-aa-001', 'flight-aa123-001', 'airline-aa-001', CURRENT_TIMESTAMP()),
('fa-aa123-b6-001', 'flight-aa123-001', 'airline-b6-001', CURRENT_TIMESTAMP()), -- Codeshare with JetBlue
('fa-aa456-aa-001', 'flight-aa456-001', 'airline-aa-001', CURRENT_TIMESTAMP()),
('fa-aa789-aa-001', 'flight-aa789-001', 'airline-aa-001', CURRENT_TIMESTAMP()),
('fa-aa101-aa-001', 'flight-aa101-001', 'airline-aa-001', CURRENT_TIMESTAMP()),

-- Delta flights
('fa-dl789-dl-001', 'flight-dl789-001', 'airline-dl-001', CURRENT_TIMESTAMP()),
('fa-dl234-dl-001', 'flight-dl234-001', 'airline-dl-001', CURRENT_TIMESTAMP()),
('fa-dl567-dl-001', 'flight-dl567-001', 'airline-dl-001', CURRENT_TIMESTAMP()),

-- United flights
('fa-ua101-ua-001', 'flight-ua101-001', 'airline-ua-001', CURRENT_TIMESTAMP()),
('fa-ua102-ua-001', 'flight-ua102-001', 'airline-ua-001', CURRENT_TIMESTAMP()),
('fa-ua303-ua-001', 'flight-ua303-001', 'airline-ua-001', CURRENT_TIMESTAMP()),
('fa-ua404-ua-001', 'flight-ua404-001', 'airline-ua-001', CURRENT_TIMESTAMP()),

-- JetBlue flights
('fa-b6-501-b6-001', 'flight-b6-501-001', 'airline-b6-001', CURRENT_TIMESTAMP()),
('fa-b6-502-b6-001', 'flight-b6-502-001', 'airline-b6-001', CURRENT_TIMESTAMP()),

-- Southwest flights
('fa-wn-601-wn-001', 'flight-wn-601-001', 'airline-wn-001', CURRENT_TIMESTAMP()),
('fa-wn-602-wn-001', 'flight-wn-602-001', 'airline-wn-001', CURRENT_TIMESTAMP()),

-- International flights
('fa-ba-701-ba-001', 'flight-ba-701-001', 'airline-ba-001', CURRENT_TIMESTAMP()),
('fa-af-801-af-001', 'flight-af-801-001', 'airline-af-001', CURRENT_TIMESTAMP()),
('fa-jl-901-jl-001', 'flight-jl-901-001', 'airline-jl-001', CURRENT_TIMESTAMP());

-- Insert sample fares
INSERT INTO fares (id, airline_id, base_price, fare_name, description, created_at, updated_at) VALUES
-- American Airlines fares
('fare-aa-standard-001', 'airline-aa-001', 200.00, 'Standard Fare', 'Any flight has fare $200', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-aa-bos-special-001', 'airline-aa-001', 150.00, 'BOS Special', 'Any flight departing/arriving at BOS has fare $150', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-aa-early-bird-001', 'airline-aa-001', 175.00, 'Early Bird', 'Any flight leaving before 09:00 has fare $175', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-aa-multi-leg-001', 'airline-aa-001', 160.00, 'Multi-Leg Discount', 'Any flight as part of a 2-or-more leg trip has fare $160', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-aa-dfw-hub-001', 'airline-aa-001', 130.00, 'DFW Hub Special', 'Any flight departing/arriving at DFW has fare $130', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- Delta fares
('fare-dl-standard-001', 'airline-dl-001', 220.00, 'Standard Fare', 'Any flight has fare $220', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-dl-jfk-special-001', 'airline-dl-001', 180.00, 'JFK Special', 'Any flight departing/arriving at JFK has fare $180', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-dl-atl-hub-001', 'airline-dl-001', 140.00, 'ATL Hub Special', 'Any flight departing/arriving at ATL has fare $140', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-dl-west-coast-001', 'airline-dl-001', 200.00, 'West Coast Special', 'Any flight to/from LAX has fare $200', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- United fares
('fare-ua-standard-001', 'airline-ua-001', 190.00, 'Standard Fare', 'Any flight has fare $190', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-ua-ord-hub-001', 'airline-ua-001', 140.00, 'ORD Hub', 'Any flight departing/arriving at ORD has fare $140', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-ua-sfo-hub-001', 'airline-ua-001', 150.00, 'SFO Hub', 'Any flight departing/arriving at SFO has fare $150', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-ua-den-hub-001', 'airline-ua-001', 160.00, 'DEN Hub', 'Any flight departing/arriving at DEN has fare $160', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- JetBlue fares
('fare-b6-standard-001', 'airline-b6-001', 180.00, 'Standard Fare', 'Any flight has fare $180', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-b6-jfk-hub-001', 'airline-b6-001', 120.00, 'JFK Hub Special', 'Any flight departing/arriving at JFK has fare $120', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-b6-bos-hub-001', 'airline-b6-001', 130.00, 'BOS Hub Special', 'Any flight departing/arriving at BOS has fare $130', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- Southwest fares
('fare-wn-standard-001', 'airline-wn-001', 160.00, 'Standard Fare', 'Any flight has fare $160', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-wn-las-hub-001', 'airline-wn-001', 100.00, 'LAS Hub Special', 'Any flight departing/arriving at LAS has fare $100', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-wn-den-hub-001', 'airline-wn-001', 110.00, 'DEN Hub Special', 'Any flight departing/arriving at DEN has fare $110', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- International airline fares
('fare-ba-standard-001', 'airline-ba-001', 800.00, 'Standard Fare', 'Any international flight has fare $800', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-af-standard-001', 'airline-af-001', 750.00, 'Standard Fare', 'Any international flight has fare $750', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('fare-jl-standard-001', 'airline-jl-001', 900.00, 'Standard Fare', 'Any international flight has fare $900', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Insert fare restrictions
INSERT INTO fare_restrictions (id, fare_id, restriction_type, restriction_value, created_at) VALUES
-- American Airlines restrictions
('restriction-bos-special-001', 'fare-aa-bos-special-001', 'ENDPOINT', 'BOS', CURRENT_TIMESTAMP()),
('restriction-early-bird-001', 'fare-aa-early-bird-001', 'DEPARTURE_TIME', '09:00', CURRENT_TIMESTAMP()),
('restriction-multi-leg-001', 'fare-aa-multi-leg-001', 'MULTI_LEG', '2', CURRENT_TIMESTAMP()),
('restriction-dfw-hub-001', 'fare-aa-dfw-hub-001', 'ENDPOINT', 'DFW', CURRENT_TIMESTAMP()),

-- Delta restrictions
('restriction-jfk-special-001', 'fare-dl-jfk-special-001', 'ENDPOINT', 'JFK', CURRENT_TIMESTAMP()),
('restriction-atl-hub-001', 'fare-dl-atl-hub-001', 'ENDPOINT', 'ATL', CURRENT_TIMESTAMP()),
('restriction-west-coast-001', 'fare-dl-west-coast-001', 'ENDPOINT', 'LAX', CURRENT_TIMESTAMP()),

-- United restrictions
('restriction-ord-hub-001', 'fare-ua-ord-hub-001', 'ENDPOINT', 'ORD', CURRENT_TIMESTAMP()),
('restriction-sfo-hub-001', 'fare-ua-sfo-hub-001', 'ENDPOINT', 'SFO', CURRENT_TIMESTAMP()),
('restriction-den-hub-001', 'fare-ua-den-hub-001', 'ENDPOINT', 'DEN', CURRENT_TIMESTAMP()),

-- JetBlue restrictions
('restriction-jfk-hub-001', 'fare-b6-jfk-hub-001', 'ENDPOINT', 'JFK', CURRENT_TIMESTAMP()),
('restriction-bos-hub-001', 'fare-b6-bos-hub-001', 'ENDPOINT', 'BOS', CURRENT_TIMESTAMP()),

-- Southwest restrictions
('restriction-las-hub-001', 'fare-wn-las-hub-001', 'ENDPOINT', 'LAS', CURRENT_TIMESTAMP()),
('restriction-den-hub-wn-001', 'fare-wn-den-hub-001', 'ENDPOINT', 'DEN', CURRENT_TIMESTAMP());

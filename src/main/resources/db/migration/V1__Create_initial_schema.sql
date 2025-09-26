-- SQLite-compatible schema for Flight Search Engine

-- Users table for authentication
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'ADMIN' CHECK (role IN ('ADMIN')),
    assigned_airline_code TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Airlines table
CREATE TABLE airlines (
    id TEXT PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    country TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for airlines
CREATE INDEX idx_airlines_code ON airlines(code);

-- Airports table
CREATE TABLE airports (
    id TEXT PRIMARY KEY,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    city TEXT NOT NULL,
    country TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for airports
CREATE INDEX idx_airports_code ON airports(code);
CREATE INDEX idx_airports_city ON airports(city);

-- Flights table
CREATE TABLE flights (
    id TEXT PRIMARY KEY,
    flight_number TEXT NOT NULL,
    source_airport_id TEXT NOT NULL,
    destination_airport_id TEXT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_airport_id) REFERENCES airports(id),
    FOREIGN KEY (destination_airport_id) REFERENCES airports(id)
);

-- Create indexes for flights
CREATE INDEX idx_flights_source_destination ON flights(source_airport_id, destination_airport_id);
CREATE INDEX idx_flights_departure_time ON flights(departure_time);
CREATE INDEX idx_flights_arrival_time ON flights(arrival_time);

-- Flight-Airline codeshare relationship
CREATE TABLE flight_airlines (
    id TEXT PRIMARY KEY,
    flight_id TEXT NOT NULL,
    airline_id TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_id) REFERENCES flights(id) ON DELETE CASCADE,
    FOREIGN KEY (airline_id) REFERENCES airlines(id) ON DELETE CASCADE,
    UNIQUE (flight_id, airline_id)
);

-- Create indexes for flight_airlines
CREATE INDEX idx_flight_airlines_flight_id ON flight_airlines(flight_id);
CREATE INDEX idx_flight_airlines_airline_id ON flight_airlines(airline_id);

-- Fares table
CREATE TABLE fares (
    id TEXT PRIMARY KEY,
    airline_id TEXT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    fare_name TEXT NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airline_id) REFERENCES airlines(id) ON DELETE CASCADE
);

-- Create indexes for fares
CREATE INDEX idx_fares_airline_id ON fares(airline_id);
CREATE INDEX idx_fares_base_price ON fares(base_price);

-- Fare restrictions table
CREATE TABLE fare_restrictions (
    id TEXT PRIMARY KEY,
    fare_id TEXT NOT NULL,
    restriction_type TEXT NOT NULL CHECK (restriction_type IN ('ENDPOINT', 'DEPARTURE_TIME', 'MULTI_LEG')),
    restriction_value TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fare_id) REFERENCES fares(id) ON DELETE CASCADE
);

-- Create indexes for fare_restrictions
CREATE INDEX idx_fare_restrictions_fare_id ON fare_restrictions(fare_id);
CREATE INDEX idx_fare_restrictions_type ON fare_restrictions(restriction_type);

-- User sessions table for JWT management
CREATE TABLE user_sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    token_hash TEXT NOT NULL,
    refresh_token_hash TEXT NOT NULL,
    expires_at DATETIME NOT NULL,
    refresh_expires_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for user_sessions
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
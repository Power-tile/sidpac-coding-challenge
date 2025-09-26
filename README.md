# Flight Search Engine

A comprehensive flight search engine built with Spring Boot and SQLite, featuring JWT authentication, role-based access control, and airline-specific admin permissions.

## Features

- **Flight Management**: Create, read, update, and delete flights with codeshare airline support
- **Fare Management**: Complex fare pricing with restrictions (endpoint, time-based, multi-leg)
- **Flight Search**: Advanced search with pricing calculations and multi-leg trip support
- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Airline-Specific Permissions**: Admins can be assigned to specific airlines for management
- **Zero-Configuration Database**: Uses SQLite - no database server setup required!
- **Comprehensive Testing**: Unit, integration, and end-to-end tests
- **CI/CD Pipeline**: Automated testing and deployment

## Prerequisites

- **Java 17+** - Download from [adoptium.net](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Maven 3.6+** - Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- **Git** (optional) - Download from [git-scm.com](https://git-scm.com/downloads)
- **No database server required** - Uses SQLite (embedded database)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd sidpac-coding-challenge
```

### 2. Initialize Database

**Important**: For first-time setup, you need to manually initialize the database with the complete schema and sample data.

```bash
# Create dir for SQLite DB
mkdir -p data
# Initialize the database with schema and sample data
sqlite3 data/flight_search.db < scripts/init-database.sql
```

This command will:
- Create all database tables (users, airlines, airports, flights, fares, etc.)
- Insert comprehensive sample data including:
  - 20 airports (US and international)
  - 16 airlines (US and international carriers)
  - 18 sample flights with realistic schedules
  - 25+ fare rules with complex pricing restrictions
  - 5 admin users (1 super admin + 4 airline-specific admins)
  - Codeshare relationships between airlines

**Note**: This is a complete database initialization script that must be run manually for initial setup. Spring Boot will not automatically load any data files.

### 3. Build and Run the Application

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Endpoints (with permissions)

Below is a high-signal summary of the available endpoints, their HTTP methods, authentication requirements, and typical usage.

- Authentication (public)
  - POST `/api/auth/register` — Register a new admin (super-admin or airline-admin)
  - POST `/api/auth/login` — Obtain session ID for authentication
  - POST `/api/auth/logout` — Invalidate the current session (requires X-Session-ID header)

- Flights (read: public; write: ADMIN only)
  - GET `/api/flights` — List flights (public)
  - GET `/api/flights/{id}` — Get a flight by id (public)
  - GET `/api/flights/airline/{code}` — List flights for an airline (public)
  - GET `/api/flights/search?source=AAA&destination=BBB[&departureTime=ISO]` — Search flights (public)
  - POST `/api/flights` — Create flight (ADMIN; airline-specific checks enforced)
  - PUT `/api/flights/{id}` — Update flight (ADMIN)
  - DELETE `/api/flights/{id}` — Delete flight (ADMIN)

- Flight Search (public)
  - POST `/api/search/flights` — Search and return priced trips by airline (public)

### Permissions model
- Only one role exists: ADMIN.
  - Super Admin: `assignedAirlineCode` is null; can manage all airlines.
  - Airline Admin: `assignedAirlineCode` is set; can only modify that airline's data.
- All read/search endpoints listed above are public and do not require authentication.
- Write endpoints (POST/PUT/DELETE on flights) require an ADMIN session ID (X-Session-ID header) and are enforced by airline scope.

### Quick examples

- Register (creates an ADMIN; set `assignedAirlineCode` later via admin tools if needed)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

- Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "password123"
  }'
```

- Public: list flights
```bash
curl -X GET "http://localhost:8080/api/flights"
```

- Public: search priced trips
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "BOS", "destinationAirport": "LAX"}'
```

- ADMIN: create flight (replace SESSION_ID)
```bash
curl -X POST http://localhost:8080/api/flights \
  -H "X-Session-ID: SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA555",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "LAX",
    "departureTime": "2024-03-20T09:30:00",
    "arrivalTime": "2024-03-20T15:30:00",
    "airlineCodes": ["AA"]
  }'
```

### 4. Verify Database Setup

You can verify the database was initialized correctly by checking the data:

```bash
# Check if database exists and has data
sqlite3 data/flight_search.db "SELECT COUNT(*) as airports FROM airports;"
sqlite3 data/flight_search.db "SELECT COUNT(*) as airlines FROM airlines;"
sqlite3 data/flight_search.db "SELECT COUNT(*) as flights FROM flights;"
sqlite3 data/flight_search.db "SELECT COUNT(*) as users FROM users;"
```

Expected results:
- Airports: 20
- Airlines: 16  
- Flights: 18
- Users: 5

### 5. Access API Documentation

Once the application is running, you can access:

### 6. Initial Data and Test Users

The database comes pre-loaded with comprehensive test data:

#### Test Users (Password: `password123`)
- **Super Admin**: `admin` / `admin@flightsearch.com` - Full system access
- **American Airlines Admin**: `aa_admin` / `aa_admin@flightsearch.com` - AA airline management
- **Delta Admin**: `dl_admin` / `dl_admin@flightsearch.com` - DL airline management  
- **United Admin**: `ua_admin` / `ua_admin@flightsearch.com` - UA airline management
- **JetBlue Admin**: `b6_admin` / `b6_admin@flightsearch.com` - B6 airline management

#### Sample Data Includes:
- **20 Airports**: Major US and international airports (BOS, LAX, JFK, LHR, etc.)
- **16 Airlines**: US and international carriers (AA, DL, UA, B6, BA, AF, etc.)
- **18 Flights**: Realistic flight schedules for March 20, 2024
- **25+ Fares**: Complex pricing with restrictions (hub specials, early bird, multi-leg)
- **Codeshare Relationships**: Flights operated by multiple airlines

#### Test Flight Search Examples:
```bash
# Search BOS to LAX
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "BOS", "destinationAirport": "LAX"}'

# Search JFK to LAX  
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "JFK", "destinationAirport": "LAX"}'
```

## Configuration

### Environment Variables

You can override default configuration using environment variables:

```bash
# JWT configuration
export JWT_SECRET=your_secret_key_here

# Server port
export SERVER_PORT=8080
```

## Comprehensive API Usage Examples

This section provides working examples using the actual sample data loaded in the database. All examples are based on the flights, airlines, and fares created by the initialization script.

### Authentication

#### 1. Register a new admin user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_admin",
    "email": "new_admin@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "Admin"
  }'
```

**Expected Response:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "refreshToken": null,
  "role": "ADMIN"
}
```

#### 2. Login with existing test user
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440001",
  "refreshToken": null,
  "role": "ADMIN"
}
```

#### 3. Logout (requires session ID from login)
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "X-Session-ID: 550e8400-e29b-41d4-a716-446655440001"
```

**Expected Response:** `200 OK` (empty body)

### Flight Management (Public Endpoints)

#### 4. List all flights
```bash
curl -X GET "http://localhost:8080/api/flights"
```

**Expected Response:** Array of 18 flights with details like:
```json
[
  {
    "id": "flight-aa123-001",
    "flightNumber": "AA123",
    "sourceAirport": {
      "id": "airport-bos-001",
      "code": "BOS",
      "name": "Logan International Airport",
      "city": "Boston",
      "country": "USA"
    },
    "destinationAirport": {
      "id": "airport-lax-001", 
      "code": "LAX",
      "name": "Los Angeles International Airport",
      "city": "Los Angeles",
      "country": "USA"
    },
    "departureTime": "2024-03-20T09:30:00",
    "arrivalTime": "2024-03-20T15:30:00",
    "airlines": [
      {
        "id": "airline-aa-001",
        "code": "AA",
        "name": "American Airlines",
        "country": "USA"
      },
      {
        "id": "airline-b6-001",
        "code": "B6", 
        "name": "JetBlue Airways",
        "country": "USA"
      }
    ]
  }
]
```

#### 5. Get specific flight by ID
```bash
curl -X GET "http://localhost:8080/api/flights/flight-aa123-001"
```

#### 6. Search flights by route (BOS to LAX)
```bash
curl -X GET "http://localhost:8080/api/flights/search?source=BOS&destination=LAX"
```

**Expected Response:** Array of flights from BOS to LAX, including:
- AA123 (American Airlines, 09:30-15:30)
- DL789 (Delta, 10:00-16:30) 
- B6-501 (JetBlue, 16:00-19:30)

#### 7. Search flights by route and time
```bash
curl -X GET "http://localhost:8080/api/flights/search?source=BOS&destination=LAX&departureTime=2024-03-20T09:30:00"
```

#### 8. Get flights by airline
```bash
curl -X GET "http://localhost:8080/api/flights/airline/AA"
```

**Expected Response:** Array of American Airlines flights including:
- AA123 (BOS→LAX)
- AA456 (LAX→BOS)
- AA789 (JFK→MIA)
- AA101 (DFW→LAX)

### Advanced Flight Search with Pricing

#### 9. Search for priced trips (BOS to LAX)
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAirport": "BOS",
    "destinationAirport": "LAX"
  }'
```

**Expected Response:**
```json
{
  "trips": [
    {
      "airline": "AA",
      "totalPrice": 150.00,
      "totalDuration": 360,
      "flights": [
        {
          "id": "flight-aa123-001",
          "flightNumber": "AA123",
          "sourceAirport": {
            "code": "BOS",
            "name": "Logan International Airport"
          },
          "destinationAirport": {
            "code": "LAX", 
            "name": "Los Angeles International Airport"
          },
          "departureTime": "2024-03-20T09:30:00",
          "arrivalTime": "2024-03-20T15:30:00"
        }
      ]
    },
    {
      "airline": "DL",
      "totalPrice": 200.00,
      "totalDuration": 390,
      "flights": [
        {
          "id": "flight-dl789-001",
          "flightNumber": "DL789",
          "sourceAirport": {"code": "JFK"},
          "destinationAirport": {"code": "LAX"},
          "departureTime": "2024-03-20T10:00:00",
          "arrivalTime": "2024-03-20T16:30:00"
        }
      ]
    }
  ],
  "searchCriteria": {
    "sourceAirport": "BOS",
    "destinationAirport": "LAX",
    "departureTime": null
  },
  "totalResults": 2
}
```

#### 10. Search with specific departure time
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAirport": "BOS",
    "destinationAirport": "LAX",
    "departureTime": "2024-03-20T09:30:00"
  }'
```

#### 11. Search for multi-leg trips (BOS to LAX via ORD)
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAirport": "BOS",
    "destinationAirport": "LAX"
  }'
```

**Expected Response:** May include multi-leg trips like:
```json
{
  "airline": "UA",
  "totalPrice": 280.00,
  "totalDuration": 450,
  "flights": [
    {
      "flightNumber": "UA101",
      "sourceAirport": {"code": "BOS"},
      "destinationAirport": {"code": "ORD"},
      "departureTime": "2024-03-20T08:00:00",
      "arrivalTime": "2024-03-20T10:30:00"
    },
    {
      "flightNumber": "UA102", 
      "sourceAirport": {"code": "ORD"},
      "destinationAirport": {"code": "LAX"},
      "departureTime": "2024-03-20T12:00:00",
      "arrivalTime": "2024-03-20T15:00:00"
    }
  ]
}
```

### Flight Management (Admin Endpoints)

#### 12. Create a new flight (requires admin session)
```bash
# First login to get session ID
SESSION_ID=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "password123"}' | \
  jq -r '.token')

# Create flight using session ID
curl -X POST http://localhost:8080/api/flights \
  -H "X-Session-ID: $SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA999",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "SFO",
    "departureTime": "2024-03-20T11:00:00",
    "arrivalTime": "2024-03-20T17:30:00",
    "airlineCodes": ["AA"]
  }'
```

**Expected Response:** `201 Created` with flight details

#### 13. Update an existing flight
```bash
curl -X PUT http://localhost:8080/api/flights/flight-aa123-001 \
  -H "X-Session-ID: $SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA123",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "LAX",
    "departureTime": "2024-03-20T10:00:00",
    "arrivalTime": "2024-03-20T16:00:00",
    "airlineCodes": ["AA", "B6"]
  }'
```

#### 14. Delete a flight
```bash
curl -X DELETE http://localhost:8080/api/flights/flight-aa123-001 \
  -H "X-Session-ID: $SESSION_ID"
```

**Expected Response:** `200 OK` (empty body)

### Error Handling Examples

#### 15. Invalid authentication
```bash
curl -X POST http://localhost:8080/api/flights \
  -H "X-Session-ID: invalid-session-id" \
  -H "Content-Type: application/json" \
  -d '{"flightNumber": "AA999", "sourceAirportCode": "BOS", "destinationAirportCode": "SFO", "departureTime": "2024-03-20T11:00:00", "arrivalTime": "2024-03-20T17:30:00", "airlineCodes": ["AA"]}'
```

**Expected Response:** `401 Unauthorized`

#### 16. Insufficient permissions (airline admin trying to modify other airline)
```bash
# Login as AA admin
AA_SESSION_ID=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "aa_admin", "password": "password123"}' | \
  jq -r '.token')

# Try to create Delta flight (should fail)
curl -X POST http://localhost:8080/api/flights \
  -H "X-Session-ID: $AA_SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "DL999",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "LAX",
    "departureTime": "2024-03-20T11:00:00",
    "arrivalTime": "2024-03-20T17:30:00",
    "airlineCodes": ["DL"]
  }'
```

**Expected Response:** `403 Forbidden`

#### 17. Invalid airport codes
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAirport": "INVALID",
    "destinationAirport": "LAX"
  }'
```

**Expected Response:** `400 Bad Request`

### Sample Data Search Examples

#### 18. Search popular routes with expected results

**BOS to LAX (3 direct flights):**
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "BOS", "destinationAirport": "LAX"}'
```

**JFK to LAX (2 direct flights):**
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "JFK", "destinationAirport": "LAX"}'
```

**International routes:**
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "LHR", "destinationAirport": "JFK"}'
```

**Hub-to-hub connections:**
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{"sourceAirport": "ATL", "destinationAirport": "LAX"}'
```

### Understanding the Pricing System

The flight search engine uses a sophisticated fare pricing system with multiple fare types and restrictions. Here's how pricing works:

#### Fare Types and Restrictions

**1. Standard Fares** - Base pricing for any flight
- American Airlines: $200
- Delta: $220  
- United: $190
- JetBlue: $180
- Southwest: $160

**2. Hub Specials** - Discounted pricing for hub airports
- **BOS Hub Special**: $150 (American Airlines), $130 (JetBlue)
- **DFW Hub Special**: $130 (American Airlines)
- **ATL Hub Special**: $140 (Delta)
- **ORD Hub Special**: $140 (United)
- **SFO Hub Special**: $150 (United)
- **DEN Hub Special**: $160 (United)
- **JFK Hub Special**: $120 (JetBlue), $180 (Delta)
- **LAS Hub Special**: $100 (Southwest)
- **DEN Hub Special**: $110 (Southwest)

**3. Time-Based Restrictions**
- **Early Bird Special**: $175 (American Airlines) for flights departing before 09:00

**4. Multi-Leg Discounts**
- **Multi-Leg Discount**: $160 (American Airlines) for flights that are part of 2+ leg trips

#### How Pricing is Calculated

1. **Direct Flights**: Uses the best applicable fare for the route
2. **Multi-Leg Trips**: Each leg is priced separately, then combined
3. **Fare Selection**: The system automatically selects the lowest applicable fare based on:
   - Airport endpoints (hub specials)
   - Departure time (early bird discounts)
   - Trip complexity (multi-leg discounts)

#### Example Pricing Scenarios

**BOS → LAX (American Airlines AA123)**
- Standard fare: $200
- BOS Hub Special: $150 ✅ (applies because departing from BOS)
- **Final Price: $150**

**JFK → LAX (Delta DL789)**  
- Standard fare: $220
- JFK Special: $180 ✅ (applies because departing from JFK)
- **Final Price: $180**

**BOS → ORD → LAX (United multi-leg)**
- BOS → ORD: ORD Hub Special = $140
- ORD → LAX: ORD Hub Special = $140  
- Multi-leg discount: $160 per leg
- **Final Price: $280** (140 + 140)

**Early morning BOS → LAX (American Airlines)**
- Standard fare: $200
- BOS Hub Special: $150
- Early Bird Special: $175 (departure before 09:00)
- **Final Price: $150** (lowest applicable fare)

### Testing the API Examples

To test these examples, follow these steps:

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Verify the database is initialized:**
   ```bash
   sqlite3 data/flight_search.db "SELECT COUNT(*) as flights FROM flights;"
   # Should return: 18
   ```

3. **Test a simple search:**
   ```bash
   curl -X POST http://localhost:8080/api/search/flights \
     -H "Content-Type: application/json" \
     -d '{"sourceAirport": "BOS", "destinationAirport": "LAX"}' | jq
   ```

4. **Test authentication:**
   ```bash
   # Login
   SESSION_ID=$(curl -s -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail": "admin", "password": "password123"}' | \
     jq -r '.token')
   
   # Use session for admin operations
   curl -X GET "http://localhost:8080/api/flights" \
     -H "X-Session-ID: $SESSION_ID" | jq
   ```

#### Expected Results Summary

Based on the sample data, you should see:

- **18 total flights** across 16 airlines
- **20 airports** (US and international)
- **Multiple fare options** with hub specials and discounts
- **Codeshare flights** (e.g., AA123 is also operated by JetBlue)
- **Multi-leg trip options** for complex routes
- **International flights** (LHR→JFK, CDG→JFK, NRT→LAX)

#### Troubleshooting API Examples

**If you get 404 errors:**
- Ensure the application is running on port 8080
- Check that the database was initialized properly

**If you get 401/403 errors:**
- Verify you're using the correct session ID format
- Check that the user has the required permissions

**If search returns no results:**
- Verify airport codes are 3-letter uppercase (BOS, LAX, etc.)
- Check that the departure date matches the sample data (2024-03-20)

### User Roles and Permissions

#### Role Types:
- **ADMIN**: Only role. If no assigned airline, full system admin; if assigned an airline code, can modify that airline only.

#### Airline-Specific Admin Permissions:
- Super Admin (no assigned airline): Can manage all airlines
- Airline Admin (assigned airline): Can only manage their assigned airline

## Testing

### Run All Tests
```bash
mvn test
```

## Database Schema

### Core Tables:
- **users**: User accounts with role-based permissions
- **airlines**: Airline information and codeshare relationships
- **airports**: Airport codes and locations
- **flights**: Point-to-point flight information
- **fares**: Pricing rules with restrictions
- **fare_restrictions**: Specific fare limitations
- **user_sessions**: JWT session management

### Key Relationships:
- Flights can have multiple airlines (codeshare)
- Fares belong to airlines and can have multiple restrictions
- Users can be assigned to specific airlines for admin access

### Table Details

| Table | Columns | Description |
|-------|---------|-------------|
| **users** | `id, username, email, password_hash, first_name, last_name, role, assigned_airline_code, created_at, updated_at` | User accounts with ADMIN role and optional airline assignment |
| **airlines** | `id, code, name, country, created_at, updated_at` | Airline information with unique codes (AA, DL, UA, etc.) |
| **airports** | `id, code, name, city, country, created_at, updated_at` | Airport data with IATA codes (BOS, LAX, JFK, etc.) |
| **flights** | `id, flight_number, source_airport_id, destination_airport_id, departure_time, arrival_time, created_at, updated_at` | Flight schedules with source/destination airports and timing |
| **flight_airlines** | `id, flight_id, airline_id, created_at` | Many-to-many relationship for codeshare flights (one flight, multiple airlines) |
| **fares** | `id, airline_id, base_price, fare_name, description, created_at, updated_at` | Pricing rules per airline with base prices and descriptions |
| **fare_restrictions** | `id, fare_id, restriction_type, restriction_value, created_at` | Fare limitations (ENDPOINT, DEPARTURE_TIME, MULTI_LEG) |
| **user_sessions** | `id, user_id, token_hash, refresh_token_hash, expires_at, refresh_expires_at, created_at` | JWT session management with access/refresh tokens |

## Development

### Project Structure
```
src/
├── main/
│   ├── java/edu/mit/sidpac/flightsearch/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.yml # Configuration
│       └── db/migration/   # Database migrations
└── test/                   # Test classes
```

### Adding New Features

1. **Create Entity**: Add JPA entity in `entity/` package
2. **Create Repository**: Add repository interface in `repository/` package
3. **Create Service**: Add business logic in `service/` package
4. **Create Controller**: Add REST endpoints in `controller/` package
5. **Add Tests**: Create corresponding test classes
6. **Update Documentation**: Update README and API docs

## Troubleshooting

### Common Issues

#### 1. Database Connection Error
```
Error: Could not create connection to database server
```
**Solution**: Ensure the `data/` directory exists and is writable. The SQLite database will be created automatically.

#### 2. Port Already in Use
```
Error: Port 8080 was already in use
```
**Solution**: Change port in `application.yml` or stop the process using port 8080

#### 3. Maven Build Fails
```
Error: Could not resolve dependencies
```
**Solution**: Check internet connection and Maven settings, try `mvn clean install -U`


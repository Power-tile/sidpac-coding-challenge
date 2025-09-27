# Flight Search Engine

A comprehensive flight search engine built with Spring Boot and SQLite, featuring session-based authentication, role-based access control, and airline-specific admin permissions.

This is a project built for the SidPac @ MIT Web Chair Challenge -> Backend.

## Features

- **Flight Management**: Create, read, update, and delete flights with codeshare airline support
- **Fare Management**: Complex fare pricing with restrictions (endpoint, time-based, multi-leg)
- **Flight Search**: Advanced search with pricing calculations and multi-leg trip support
- **Authentication & Authorization**: session-based authentication with role-based access control
- **Airline-Specific Permissions**: Admins can be assigned to specific airlines for management
- **Comprehensive Testing**: Unit, integration, and end-to-end tests (see test section!)

### Next Steps in Development

- Multi-leg flight options that exceeds two legs.
- More adequate documentations for API output types and structure & classes.
- API for airlines' fare restrictions CRUD.
- Web API for easier information setup.

## Prerequisites

- **Java 17+** - Download from [adoptium.net](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Maven 3.6+** - Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- **Git** (optional) - Download from [git-scm.com](https://git-scm.com/downloads)
- **No database server required** - Uses SQLite (embedded database)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Power-tile/sidpac-coding-challenge.git
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

# Run the application
mvn spring-boot:run

# Or, to run tests (There is a comprehensive list of all test cases later below)
mvn test
```

The application will start on `http://localhost:8080/api`

## API Endpoints (with permissions)

Below is a high-signal summary of the available endpoints, their HTTP methods, authentication requirements, and typical usage.

- Authentication
  - POST `/api/auth/register` — Register a new admin (SUPER ADMIN only; requires X-Session-ID header)
  - POST `/api/auth/login` — Obtain session ID for authentication (public)
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
  - GET `/api/flights/planning` — Search and return priced trips by airline (public)

### Error Responses

The API returns appropriate HTTP status codes for different scenarios:

- **404 Not Found**: Returned when requesting a resource that doesn't exist
  - `GET /api/flights/{id}` — When flight ID doesn't exist
  - `GET /api/flights/airline/{code}` — When airline code doesn't exist
- **401 Unauthorized**: Returned when authentication is required but not provided
- **403 Forbidden**: Returned when user lacks permissions for the requested operation
- **400 Bad Request**: Returned for invalid request data or parameters

### Permissions model
- Only one role exists: ADMIN.
  - Super Admin: `assignedAirlineCode` is null; can manage all airlines.
  - Airline Admin: `assignedAirlineCode` is set; can only modify that airline's data.
- All read/search endpoints listed above are public and do not require authentication.
- Write endpoints (POST/PUT/DELETE on flights) require an ADMIN session ID (X-Session-ID header) and are enforced by airline scope.

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

## Comprehensive API Usage Examples

This section provides working examples using the actual sample data loaded in the database. All examples are based on the flights, airlines, and fares created by the initialization script.

### Advanced Flight Search Guide

The flight search engine provides comprehensive search capabilities with pricing, multi-leg routing, and time-based filtering. All search endpoints are public and do not require authentication.

#### Search Endpoints

- **GET** `/api/flights/planning` - Advanced search with pricing and multi-leg support
- **GET** `/api/flights/search?source=AAA&destination=BBB[&departureTime=ISO]` - Basic flight search

#### Search Request Format

**Query Parameters:**
- `sourceAirport` (required): 3-letter airport code (e.g., "BOS")
- `destinationAirport` (required): 3-letter airport code (e.g., "LAX")  
- `departureTime` (optional): ISO datetime format (e.g., "2024-03-20T09:30:00")

**Example URL:**
```
GET /api/flights/planning?sourceAirport=BOS&destinationAirport=LAX&departureTime=2024-03-20T09:30:00
```

### Search Capabilities

#### ✅ Multi-Leg Flights
The system automatically finds both direct flights and connecting flights:
- **Direct flights**: Single flight from source to destination
- **Connecting flights**: 2+ leg trips with layovers at intermediate airports
- **Common airline requirement**: Connecting flights must use the same airline for all legs

#### ✅ Price Information
Each trip includes calculated pricing based on fare rules:
- **Hub specials**: Discounted pricing for airline hub airports
- **Time-based discounts**: Early bird specials for morning departures
- **Multi-leg discounts**: Special pricing for connecting flights
- **Automatic fare selection**: System chooses the lowest applicable fare

#### ✅ Departure Time Filtering
- **Time-based filtering**: Shows only flights departing after specified time
- **1-hour buffer**: Includes flights departing within 1 hour of specified time
- **Optional parameter**: Can be omitted for all available flights

### Search Examples

#### Basic Search (BOS to LAX)
```bash
curl -X GET "http://localhost:8080/api/flights/planning?sourceAirport=BOS&destinationAirport=LAX"
```

**Expected Response:**
```json
{
  "trips": [
    {
      "airline": "B6",
      "totalPrice": 130,
      "flights": [
        {
          "id": "flight-aa123-001",
          "createdAt": "2025-09-26T18:00:00",
          "updatedAt": "2025-09-26T18:00:00",
          "flightNumber": "AA123",
          "sourceAirport": {
            "id": "airport-bos-001",
            "createdAt": "2025-09-26T18:00:00",
            "updatedAt": "2025-09-26T18:00:00",
            "code": "BOS",
            "name": "Logan International Airport",
            "city": "Boston",
            "country": "USA",
            "location": "Boston, USA"
          },
          "destinationAirport": {
            "id": "airport-lax-001",
            "createdAt": "2025-09-26T18:00:00",
            "updatedAt": "2025-09-26T18:00:00",
            "code": "LAX",
            "name": "Los Angeles International Airport",
            "city": "Los Angeles",
            "country": "USA",
            "location": "Los Angeles, USA"
          },
          "departureTime": "2024-03-20T09:30:00",
          "arrivalTime": "2024-03-20T15:30:00",
          "flightAirlines": [
            {
              "id": "fa-aa123-b6-001",
              "createdAt": "2025-09-26T18:00:00",
              "updatedAt": null,
              "airline": {
                "id": "airline-b6-001",
                "createdAt": "2025-09-26T18:00:00",
                "updatedAt": "2025-09-26T18:00:00",
                "code": "B6",
                "name": "JetBlue Airways",
                "country": "USA"
              }
            }
          ],
          "sourceAirportCode": "BOS",
          "destinationAirportCode": "LAX",
          "durationInMinutes": 360,
          "route": "BOS → LAX",
          "airlineCode": "B6"
        }
      ],
      "totalDuration": 360,
      "route": "BOS → LAX",
      "airlineCode": "B6",
      "legCount": 1,
      "legs": [
        {
          "id": "flight-aa123-001",
          "createdAt": "2025-09-26T18:00:00",
          "updatedAt": "2025-09-26T18:00:00",
          "flightNumber": "AA123",
          "sourceAirport": {
            "id": "airport-bos-001",
            "createdAt": "2025-09-26T18:00:00",
            "updatedAt": "2025-09-26T18:00:00",
            "code": "BOS",
            "name": "Logan International Airport",
            "city": "Boston",
            "country": "USA",
            "location": "Boston, USA"
          },
          "destinationAirport": {
            "id": "airport-lax-001",
            "createdAt": "2025-09-26T18:00:00",
            "updatedAt": "2025-09-26T18:00:00",
            "code": "LAX",
            "name": "Los Angeles International Airport",
            "city": "Los Angeles",
            "country": "USA",
            "location": "Los Angeles, USA"
          },
          "departureTime": "2024-03-20T09:30:00",
          "arrivalTime": "2024-03-20T15:30:00",
          "flightAirlines": [
            {
              "id": "fa-aa123-b6-001",
              "createdAt": "2025-09-26T18:00:00",
              "updatedAt": null,
              "airline": {
                "id": "airline-b6-001",
                "createdAt": "2025-09-26T18:00:00",
                "updatedAt": "2025-09-26T18:00:00",
                "code": "B6",
                "name": "JetBlue Airways",
                "country": "USA"
              }
            }
          ],
          "sourceAirportCode": "BOS",
          "destinationAirportCode": "LAX",
          "durationInMinutes": 360,
          "route": "BOS → LAX",
          "airlineCode": "B6"
        }
      ],
      "direct": true
    }
  ],
  "searchCriteria": {
    "sourceAirport": "BOS",
    "destinationAirport": "LAX",
    "departureTime": null
  },
  "totalResults": 6
}
```

#### Search with Time Filter
```bash
curl -X GET "http://localhost:8080/api/flights/planning?sourceAirport=BOS&destinationAirport=LAX&departureTime=2024-03-20T07:00:00"
```

### Understanding the Pricing System

The flight search engine uses a sophisticated fare pricing system with multiple fare types and restrictions:

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

### Flight Management (Public Endpoints)

#### 1. List all flights
```bash
curl -X GET "http://localhost:8080/api/flights"
```

**Expected Response:** Array of 18 flights with details like:
```json
[
  {
    "id": "flight-aa123-001",
    "createdAt": "2025-09-26T18:00:00",
    "updatedAt": "2025-09-26T18:00:00",
    "flightNumber": "AA123",
    "sourceAirport": {
      "id": "airport-bos-001",
      "createdAt": "2025-09-26T18:00:00",
      "updatedAt": "2025-09-26T18:00:00",
      "code": "BOS",
      "name": "Logan International Airport",
      "city": "Boston",
      "country": "USA",
      "location": "Boston, USA"
    },
    "destinationAirport": {
      "id": "airport-lax-001",
      "createdAt": "2025-09-26T18:00:00",
      "updatedAt": "2025-09-26T18:00:00",
      "code": "LAX",
      "name": "Los Angeles International Airport",
      "city": "Los Angeles",
      "country": "USA",
      "location": "Los Angeles, USA"
    },
    "departureTime": "2024-03-20T09:30:00",
    "arrivalTime": "2024-03-20T15:30:00",
    "flightAirlines": [
      {
        "id": "fa-aa123-b6-001",
        "createdAt": "2025-09-26T18:00:00",
        "updatedAt": null,
        "airline": {
          "id": "airline-b6-001",
          "createdAt": "2025-09-26T18:00:00",
          "updatedAt": "2025-09-26T18:00:00",
          "code": "B6",
          "name": "JetBlue Airways",
          "country": "USA"
        }
      },
      {
        "id": "fa-aa123-aa-001",
        "createdAt": "2025-09-26T18:00:00",
        "updatedAt": null,
        "airline": {
          "id": "airline-aa-001",
          "createdAt": "2025-09-26T18:00:00",
          "updatedAt": "2025-09-26T18:00:00",
          "code": "AA",
          "name": "American Airlines",
          "country": "USA"
        }
      }
    ],
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "LAX",
    "durationInMinutes": 360,
    "route": "BOS → LAX",
    "airlineCode": "B6"
  }
]
```

#### 2. Get specific flight by ID
```bash
curl -X GET "http://localhost:8080/api/flights/flight-aa123-001"
```

#### 3. Search flights by route (BOS to LAX)
```bash
curl -X GET "http://localhost:8080/api/flights/search?source=BOS&destination=LAX"
```

**Expected Response:** Array of flights from BOS to LAX, including:
- AA123 (American Airlines, 09:30-15:30)
- DL789 (Delta, 10:00-16:30) 
- B6-501 (JetBlue, 16:00-19:30)

#### 4. Search flights by route and time
```bash
curl -X GET "http://localhost:8080/api/flights/search?source=BOS&destination=LAX&departureTime=2024-03-20T09:30:00"
```

#### 5. Get flights by airline
```bash
curl -X GET "http://localhost:8080/api/flights/airline/AA"
```

**Expected Response:** Array of American Airlines flights including:
- AA123 (BOS→LAX)
- AA456 (LAX→BOS)
- AA789 (JFK→MIA)
- AA101 (DFW→LAX)

### Response Format

Each search returns a `SearchResponse` with:

- **trips**: Array of available trip options, sorted by price (cheapest first)
- **searchCriteria**: The search parameters used
- **totalResults**: Number of trips found

Each trip includes:
- **airline**: Operating airline code
- **totalPrice**: Calculated total price for the trip
- **totalDuration**: Total travel time in minutes
- **flights**: Array of flight legs (1 for direct, 2+ for connecting)
- **legCount**: Number of flight legs
- **direct**: Boolean indicating if it's a direct flight

### Error Handling

- **200 OK with empty results**: Invalid airport codes or no flights found for the specified route
- **400 Bad Request**: Malformed request or invalid data format
- **500 Internal Server Error**: System error during search

### Authentication

#### 1. Register a new admin user (Super Admin only)
**Note**: Only super admins (users with no assigned airline code) can register new users. You must be logged in as a super admin to use this endpoint.

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: your-super-admin-session-id" \
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

### Flight Management (Admin Endpoints)

#### 12. Create a new flight (requires admin session, and only works for future dates)
```bash
# Create flight using session ID
curl -X POST http://localhost:8080/api/flights \
  -H "X-Session-ID: $SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA999",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "SFO",
    "departureTime": "2026-03-20T11:00:00",
    "arrivalTime": "2026-03-20T17:30:00",
    "airlineCodes": ["AA"]
  }'
```

**Expected Response:** `201 Created` with flight details. Then you can check the flight is successfully created by:

```bash
curl -X GET "http://localhost:8080/api/flights/<flight_id_from_POST_return_value>"
```

#### 13. Update an existing flight. Note departure time cannot be in the past for this.
```bash
curl -X PUT http://localhost:8080/api/flights/flight-aa123-001 \
  -H "X-Session-ID: $SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA123",
    "sourceAirportCode": "BOS",
    "destinationAirportCode": "LAX",
    "departureTime": "2026-03-20T10:00:00",
    "arrivalTime": "2026-03-20T16:00:00",
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
  -d '{"flightNumber": "AA999", "sourceAirportCode": "BOS", "destinationAirportCode": "SFO", "departureTime": "2026-03-20T11:00:00", "arrivalTime": "2026-03-20T17:30:00", "airlineCodes": ["AA"]}'
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
    "departureTime": "2026-03-20T11:00:00",
    "arrivalTime": "2026-03-20T17:30:00",
    "airlineCodes": ["DL"]
  }'
```

**Expected Response:** `403 Forbidden`

#### 17. Invalid airport codes
```bash
curl -X GET "http://localhost:8080/api/flights/planning?sourceAirport=INVALID&destinationAirport=LAX"
```

**Expected Response:** `200 OK` with empty results:
```json
{
  "trips": [],
  "searchCriteria": {
    "sourceAirport": "INVALID",
    "destinationAirport": "LAX",
    "departureTime": null
  },
  "totalResults": 0
}
```



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

### Test Coverage Summary

The Flight Search Engine includes comprehensive test coverage across multiple layers and scenarios. All tests use realistic data and cover both happy path and error conditions.

#### Test Files Overview

**1. AuthControllerTest** (`src/test/java/edu/mit/sidpac/flightsearch/controller/AuthControllerTest.java`)
- **Type**: Unit Tests (WebMvcTest with mocked services)
- **Coverage**: Authentication and registration endpoints
- **Test Scenarios**:
  - ✅ Successful login with valid credentials (username and email)
  - ✅ Login with airline admin credentials
  - ✅ Login failure with invalid credentials
  - ✅ Login failure with non-existent user
  - ✅ Successful user registration by super admin
  - ✅ Registration failure with duplicate username/email
  - ✅ Registration validation (invalid email format, weak password, missing fields)
  - ✅ Request validation (malformed JSON, empty request body)
  - ✅ Registration without session header is rejected
  - ✅ Registration with invalid session ID is rejected
  - ✅ Registration by airline admin is rejected (only super admins can register)

**2. FlightServiceTest** (`src/test/java/edu/mit/sidpac/flightsearch/service/FlightServiceTest.java`)
- **Type**: Unit Tests (Mockito with mocked dependencies)
- **Coverage**: Flight management business logic and permissions
- **Test Scenarios**:
  - ✅ Super admin can create flights for any airline
  - ✅ Airline admin can create flights for their assigned airline only
  - ✅ Airline admin cannot create flights for other airlines
  - ✅ Flight creation with codeshare airlines (multiple airlines)
  - ✅ Data validation (invalid airports, airlines, departure/arrival times)
  - ✅ Flight update with valid/insufficient permissions
  - ✅ Flight deletion with valid/insufficient permissions
  - ✅ Error handling for non-existent flights

**3. FlightSearchIntegrationTest** (`src/test/java/edu/mit/sidpac/flightsearch/integration/FlightSearchIntegrationTest.java`)
- **Type**: Integration Tests (SpringBootTest with full database)
- **Coverage**: End-to-end flight search and management functionality
- **Test Scenarios**:
  - ✅ Public flight search without authentication
  - ✅ Flight search with specific departure time filtering
  - ✅ International flight search (JFK to LHR)
  - ✅ Public access to flight listings and airline-specific flights
  - ✅ Get flights by airline code (valid airline)
  - ✅ Get flights by non-existent airline code returns 404
  - ✅ Get specific flight by ID (valid flight)
  - ✅ Get non-existent flight by ID returns 404
  - ✅ Super admin flight creation permissions
  - ✅ Airline admin permissions and restrictions
  - ✅ Unauthenticated access prevention for write operations
  - ✅ Multi-leg flight search with complex pricing
  - ✅ Database data integrity verification
  - ✅ Application context loading validation

**4. SecurityIntegrationTest** (`src/test/java/edu/mit/sidpac/flightsearch/integration/SecurityIntegrationTest.java`)
- **Type**: Integration Tests (SpringBootTest with full security configuration)
- **Coverage**: Security configuration and endpoint access control
- **Test Scenarios**:
  - ✅ Public endpoint accessibility (login, search, read operations)
  - ✅ Registration endpoint requires super admin authentication
  - ✅ Protected endpoint security (write operations require authentication)
  - ✅ Session-based authentication validation
  - ✅ Invalid/malformed session ID rejection
  - ✅ Airline-specific admin permission enforcement
  - ✅ Super admin unrestricted permissions
  - ✅ Super admin can register new users
  - ✅ Airline admin cannot register new users
  - ✅ Registration with invalid session ID is rejected
  - ✅ Registration without session header is rejected
  - ✅ Logout functionality and session invalidation
  - ✅ Database data integrity for security testing

**5. FareCalculationTest** (`src/test/java/edu/mit/sidpac/flightsearch/service/FareCalculationTest.java`)
- **Type**: Unit Tests (SpringBootTest with full database and realistic data)
- **Coverage**: Comprehensive fare calculation logic and pricing algorithms
- **Test Scenarios**:
  - ✅ Standard fare calculation (no restrictions apply)
  - ✅ Hub special fare calculations (BOS, DFW, JFK, ATL, ORD, SFO, DEN, LAS)
  - ✅ Early bird special pricing (departures before 09:00)
  - ✅ Multi-leg discount pricing (2+ leg connecting flights)
  - ✅ Lowest fare selection when multiple fares apply
  - ✅ Complex scenarios with multiple applicable restrictions
  - ✅ Fare restriction validation (ENDPOINT, DEPARTURE_TIME, MULTI_LEG)
  - ✅ Multiple restriction validation (fares with multiple conditions)
  - ✅ No applicable fares scenario handling
  - ✅ Cross-airline fare comparison (AA, DL, UA, B6, WN)
  - ✅ Numerical accuracy verification with expected pricing values

**6. FlightSearchEngineApplicationTests** (`src/test/java/edu/mit/sidpac/flightsearch/FlightSearchEngineApplicationTests.java`)
- **Type**: Application Tests (SpringBootTest with full application context)
- **Coverage**: Complete application functionality and configuration
- **Test Scenarios**:
  - ✅ Spring application context loading
  - ✅ Database data integrity (5 users, 20 airports, 16 airlines, 18 flights)
  - ✅ Authentication service functionality
  - ✅ Flight search service with realistic data
  - ✅ Flight service CRUD operations
  - ✅ Permission service for different user types
  - ✅ Fare service and pricing calculations
  - ✅ Repository functionality across all entities
  - ✅ End-to-end application workflow

#### Test Data and Environment

**Database Setup**:
- Uses SQLite test database with complete schema
- Loads comprehensive test data via `TestDatabaseSetup`
- Includes 5 admin users (1 super admin + 4 airline-specific admins)
- Contains 20 airports, 16 airlines, 18 flights, 25+ fare rules
- All tests use `@Transactional` for data isolation

**Test Configuration**:
- Separate test profile (`application-test.yml`)
- Mocked security configuration for unit tests
- Full Spring Boot context for integration tests
- JPA auditing configuration for test environment

#### Test Statistics

- **Total Test Files**: 6
- **Total Test Methods**: 100+ individual test scenarios
- **Coverage Areas**:
  - Authentication & Authorization: 20+ tests
  - Flight Management: 22+ tests (including error handling)
  - Fare Calculation & Pricing: 15+ tests
  - Security & Permissions: 20+ tests
  - Data Integrity: 10+ tests
  - End-to-End Workflows: 5+ tests
  - Error Handling & HTTP Status Codes: 8+ tests

#### Running Specific Test Categories

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test" -DfailIfNoTests=false

# Run only integration tests
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false

# Run specific test class
mvn test -Dtest="AuthControllerTest"
mvn test -Dtest="FareCalculationTest"

# Run with detailed output
mvn test -X
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


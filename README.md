# Flight Search Engine

A comprehensive flight search engine built with Spring Boot and SQLite, featuring JWT authentication, role-based access control, and airline-specific admin permissions.

## Features

- **Flight Management**: Create, read, update, and delete flights with codeshare airline support
- **Fare Management**: Complex fare pricing with restrictions (endpoint, time-based, multi-leg)
- **Flight Search**: Advanced search with pricing calculations and multi-leg trip support
- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Airline-Specific Permissions**: Admins can be assigned to specific airlines for management
- **Zero-Configuration Database**: Uses SQLite - no database server setup required!
- **API Documentation**: Swagger/OpenAPI documentation
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
  - POST `/api/auth/login` — Obtain JWT access/refresh tokens
  - POST `/api/auth/logout` — Invalidate the current session (requires Bearer token)
  - POST `/api/auth/refresh` — Obtain a new access/refresh pair (uses refresh token)

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
- Write endpoints (POST/PUT/DELETE on flights) require an ADMIN JWT and are enforced by airline scope.

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

- ADMIN: create flight (replace TOKEN)
```bash
curl -X POST http://localhost:8080/api/flights \
  -H "Authorization: Bearer TOKEN" \
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

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

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

### Application Properties

Key configuration options in `application.yml`:

```yaml
# Database (SQLite - no username/password needed)
spring:
  datasource:
    url: jdbc:sqlite:./data/flight_search.db
    driver-class-name: org.sqlite.JDBC
  sql:
    init:
      mode: never

# JWT Settings
jwt:
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

# Server
server:
  port: 8080
  servlet:
    context-path: /api
```

## API Usage

### Authentication

#### Register a new user
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

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "password123"
  }'
```

#### Search flights
```bash
curl -X POST http://localhost:8080/api/search/flights \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAirport": "BOS",
    "destinationAirport": "LAX",
    "departureTime": "2024-03-20T09:30:00"
  }'
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

### Run Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest="*UnitTest"

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# End-to-end tests only
mvn test -Dtest="*E2ETest"
```

### Test Coverage
```bash
mvn jacoco:report
```
View coverage report at: `target/site/jacoco/index.html`

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
│   ├── java/com/sidpac/flightsearch/
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

#### 3. JWT Token Invalid
```
Error: JWT signature does not match locally computed signature
```
**Solution**: Ensure JWT secret is consistent across application restarts

#### 4. Maven Build Fails
```
Error: Could not resolve dependencies
```
**Solution**: Check internet connection and Maven settings, try `mvn clean install -U`

### Logs

Application logs are available in the console output. For more detailed logging, check:
- `application.yml` for log level configuration
- Use `logging.level.com.sidpac.flightsearch=DEBUG` for detailed logs

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Run tests: `mvn test`
5. Commit changes: `git commit -m "Add feature"`
6. Push to branch: `git push origin feature-name`
7. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues:
1. Check the troubleshooting section above
2. Review the API documentation at `/api/swagger-ui.html`
3. Create an issue in the repository
4. Contact the development team

## API Reference
See the "API Endpoints (with permissions)" section above for a compact summary. Below are brief request/response notes for key operations.

- Auth: responses include `{ accessToken, refreshToken, role }` on login/register success; `400` on validation/credential errors.
- Flights: GET endpoints return either a list or a single flight entity; write endpoints return the created/updated entity or `400` on validation errors.
- Search: POST `/api/search/flights` returns a `SearchResponse` containing per-airline priced trips and legs.

### Health Check
- `GET /api/actuator/health` - Application health status

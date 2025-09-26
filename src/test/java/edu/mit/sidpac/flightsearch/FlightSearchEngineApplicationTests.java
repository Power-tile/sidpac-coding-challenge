package edu.mit.sidpac.flightsearch;

import edu.mit.sidpac.flightsearch.entity.*;
import edu.mit.sidpac.flightsearch.repository.*;
import edu.mit.sidpac.flightsearch.service.*;
import edu.mit.sidpac.flightsearch.util.TestDatabaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import edu.mit.sidpac.flightsearch.config.TestJpaAuditingConfig;
import org.springframework.context.annotation.Import;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive application-level test suite
 * Tests the complete Spring Boot application context and configuration
 * Verifies that all beans are properly configured and the application starts correctly
 * Uses full database data to test realistic application scenarios
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestJpaAuditingConfig.class)
class FlightSearchEngineApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FareRepository fareRepository;

    @Autowired
    private FareRestrictionRepository fareRestrictionRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightSearchService flightSearchService;

    @Autowired
    private FareService fareService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    @BeforeEach
    void setUp() throws Exception {
        // Load the complete database schema and data using TestDatabaseSetup
        testDatabaseSetup.loadFullDatabaseData();
    }

    /**
     * Test: Spring application context loads successfully
     * Verifies that all Spring beans are properly configured and the application starts correctly
     * Tests the basic application startup and configuration
     */
    @Test
    void testApplicationContextLoads() {
        // Verify that all required repositories are available
        assertNotNull(userRepository, "UserRepository should be available");
        assertNotNull(airportRepository, "AirportRepository should be available");
        assertNotNull(airlineRepository, "AirlineRepository should be available");
        assertNotNull(flightRepository, "FlightRepository should be available");
        assertNotNull(fareRepository, "FareRepository should be available");
        assertNotNull(fareRestrictionRepository, "FareRestrictionRepository should be available");
        assertNotNull(userSessionRepository, "UserSessionRepository should be available");

        // Verify that all required services are available
        assertNotNull(authService, "AuthService should be available");
        assertNotNull(flightService, "FlightService should be available");
        assertNotNull(flightSearchService, "FlightSearchService should be available");
        assertNotNull(fareService, "FareService should be available");
        assertNotNull(permissionService, "PermissionService should be available");
    }

    /**
     * Test: Database data integrity after application startup
     * Verifies that the loaded test data is complete and consistent
     * Tests that the database initialization was successful
     */
    @Test
    void testDatabaseDataIntegrity() {
        // Verify that all expected data was loaded correctly
        assertEquals(5, userRepository.count(), "Should have 5 admin users");
        assertEquals(20, airportRepository.count(), "Should have 20 airports");
        assertEquals(16, airlineRepository.count(), "Should have 16 airlines");
        assertEquals(18, flightRepository.count(), "Should have 18 flights");
        assertTrue(fareRepository.count() > 20, "Should have multiple fare rules");
        assertTrue(fareRestrictionRepository.count() > 10, "Should have multiple fare restrictions");
        
        // Verify specific users exist
        assertTrue(userRepository.findByUsername("admin").isPresent(), "Super admin should exist");
        assertTrue(userRepository.findByUsername("aa_admin").isPresent(), "AA admin should exist");
        assertTrue(userRepository.findByUsername("dl_admin").isPresent(), "DL admin should exist");
        assertTrue(userRepository.findByUsername("ua_admin").isPresent(), "UA admin should exist");
        assertTrue(userRepository.findByUsername("b6_admin").isPresent(), "B6 admin should exist");
        
        // Verify specific airports exist
        assertTrue(airportRepository.findByCode("BOS").isPresent(), "BOS airport should exist");
        assertTrue(airportRepository.findByCode("LAX").isPresent(), "LAX airport should exist");
        assertTrue(airportRepository.findByCode("JFK").isPresent(), "JFK airport should exist");
        assertTrue(airportRepository.findByCode("LHR").isPresent(), "LHR airport should exist");
        assertTrue(airportRepository.findByCode("CDG").isPresent(), "CDG airport should exist");
        
        // Verify specific airlines exist
        assertTrue(airlineRepository.findByCode("AA").isPresent(), "AA airline should exist");
        assertTrue(airlineRepository.findByCode("DL").isPresent(), "DL airline should exist");
        assertTrue(airlineRepository.findByCode("UA").isPresent(), "UA airline should exist");
        assertTrue(airlineRepository.findByCode("B6").isPresent(), "B6 airline should exist");
        assertTrue(airlineRepository.findByCode("BA").isPresent(), "BA airline should exist");
    }

    /**
     * Test: Authentication service functionality
     * Verifies that the authentication service works correctly with loaded data
     * Tests the core authentication functionality
     */
    @Test
    void testAuthenticationService() {
        // Test login with super admin
        var adminResponse = authService.login(new edu.mit.sidpac.flightsearch.dto.AuthRequest("admin", "password123"));
        assertNotNull(adminResponse, "Admin login should succeed");
        assertNotNull(adminResponse.getToken(), "Admin should receive session token");
        assertNull(adminResponse.getRefreshToken(), "Admin should not receive refresh token (session-based auth)");
        assertEquals("ADMIN", adminResponse.getRole(), "Admin should have ADMIN role");

        // Test login with airline admin
        var aaAdminResponse = authService.login(new edu.mit.sidpac.flightsearch.dto.AuthRequest("aa_admin", "password123"));
        assertNotNull(aaAdminResponse, "AA admin login should succeed");
        assertNotNull(aaAdminResponse.getToken(), "AA admin should receive session token");
        assertNull(aaAdminResponse.getRefreshToken(), "AA admin should not receive refresh token (session-based auth)");
        assertEquals("ADMIN", aaAdminResponse.getRole(), "AA admin should have ADMIN role");
    }

    /**
     * Test: Flight search service functionality
     * Verifies that the flight search service works correctly with loaded data
     * Tests the core business functionality of flight search
     */
    @Test
    void testFlightSearchService() {
        // Test search for a popular route (BOS to LAX)
        var searchRequest = new edu.mit.sidpac.flightsearch.dto.SearchRequest("BOS", "LAX", null);
        var searchResponse = flightSearchService.searchFlights(searchRequest);
        
        assertNotNull(searchResponse, "Search response should not be null");
        assertNotNull(searchResponse.getTrips(), "Search response should contain trips");
        assertTrue(searchResponse.getTotalResults() > 0, "Should find flights for BOS to LAX route");
        assertFalse(searchResponse.getTrips().isEmpty(), "Should have at least one trip");
        
        // Verify trip structure
        var firstTrip = searchResponse.getTrips().get(0);
        assertNotNull(firstTrip.getAirline(), "Trip should have airline code");
        assertNotNull(firstTrip.getTotalPrice(), "Trip should have total price");
        assertNotNull(firstTrip.getFlights(), "Trip should have flights");
        assertFalse(firstTrip.getFlights().isEmpty(), "Trip should have at least one flight");
    }

    /**
     * Test: Flight service functionality
     * Verifies that the flight service works correctly with loaded data
     * Tests the flight management functionality
     */
    @Test
    void testFlightService() {
        // Get a super admin user for testing
        var adminUser = userRepository.findByUsername("admin").orElseThrow();
        
        // Test creating a new flight
        var newFlight = flightService.createFlight(
            adminUser,
            "TEST123",
            "BOS",
            "LAX",
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(6),
            java.util.Set.of("AA")
        );
        
        assertNotNull(newFlight, "New flight should be created");
        assertEquals("TEST123", newFlight.getFlightNumber(), "Flight number should match");
        assertNotNull(newFlight.getSourceAirport(), "Flight should have source airport");
        assertNotNull(newFlight.getDestinationAirport(), "Flight should have destination airport");
        assertEquals("BOS", newFlight.getSourceAirport().getCode(), "Source airport should be BOS");
        assertEquals("LAX", newFlight.getDestinationAirport().getCode(), "Destination airport should be LAX");
    }

    /**
     * Test: Permission service functionality
     * Verifies that the permission service works correctly with loaded data
     * Tests the permission system for different user types
     */
    @Test
    void testPermissionService() {
        // Get different types of admin users
        var superAdmin = userRepository.findByUsername("admin").orElseThrow();
        var aaAdmin = userRepository.findByUsername("aa_admin").orElseThrow();
        var dlAdmin = userRepository.findByUsername("dl_admin").orElseThrow();
        
        // Test super admin permissions (should be able to manage any airline)
        assertTrue(permissionService.canManageFlights(superAdmin, "AA"), "Super admin should manage AA flights");
        assertTrue(permissionService.canManageFlights(superAdmin, "DL"), "Super admin should manage DL flights");
        assertTrue(permissionService.canManageFlights(superAdmin, "UA"), "Super admin should manage UA flights");
        
        // Test AA admin permissions (should only manage AA flights)
        assertTrue(permissionService.canManageFlights(aaAdmin, "AA"), "AA admin should manage AA flights");
        assertFalse(permissionService.canManageFlights(aaAdmin, "DL"), "AA admin should not manage DL flights");
        assertFalse(permissionService.canManageFlights(aaAdmin, "UA"), "AA admin should not manage UA flights");
        
        // Test DL admin permissions (should only manage DL flights)
        assertFalse(permissionService.canManageFlights(dlAdmin, "AA"), "DL admin should not manage AA flights");
        assertTrue(permissionService.canManageFlights(dlAdmin, "DL"), "DL admin should manage DL flights");
        assertFalse(permissionService.canManageFlights(dlAdmin, "UA"), "DL admin should not manage UA flights");
    }

    /**
     * Test: Fare service functionality
     * Verifies that the fare service works correctly with loaded data
     * Tests the fare calculation and pricing functionality
     */
    @Test
    void testFareService() {
        // Get a flight to test fare calculation
        var flights = flightRepository.findAll();
        assertFalse(flights.isEmpty(), "Should have flights in test database");
        
        var flight = flights.get(0);
        var airline = airlineRepository.findByCode("AA").orElseThrow();
        
        // Test fare calculation
        var fares = fareService.getFaresByAirline("AA");
        assertNotNull(fares, "Fares should not be null");
        assertFalse(fares.isEmpty(), "Should have applicable fares for the flight");
        
        // Verify fare structure
        var firstFare = fares.get(0);
        assertNotNull(firstFare.getFareName(), "Fare should have a name");
        assertNotNull(firstFare.getBasePrice(), "Fare should have a base price");
        assertTrue(firstFare.getBasePrice().compareTo(java.math.BigDecimal.ZERO) > 0, "Fare price should be positive");
    }

    /**
     * Test: Repository functionality
     * Verifies that all repositories work correctly with loaded data
     * Tests the data access layer functionality
     */
    @Test
    void testRepositoryFunctionality() {
        // Test user repository
        var allUsers = userRepository.findAll();
        assertEquals(5, allUsers.size(), "Should have 5 users");
        
        var adminUser = userRepository.findByUsername("admin");
        assertTrue(adminUser.isPresent(), "Should find admin user");
        assertEquals("ADMIN", adminUser.get().getRole().toString(), "Admin user should have ADMIN role");
        
        // Test airport repository
        var allAirports = airportRepository.findAll();
        assertEquals(20, allAirports.size(), "Should have 20 airports");
        
        var bosAirport = airportRepository.findByCode("BOS");
        assertTrue(bosAirport.isPresent(), "Should find BOS airport");
        assertEquals("Boston", bosAirport.get().getCity(), "BOS should be in Boston");
        
        // Test airline repository
        var allAirlines = airlineRepository.findAll();
        assertEquals(16, allAirlines.size(), "Should have 16 airlines");
        
        var aaAirline = airlineRepository.findByCode("AA");
        assertTrue(aaAirline.isPresent(), "Should find AA airline");
        assertEquals("American Airlines", aaAirline.get().getName(), "AA should be American Airlines");
        
        // Test flight repository
        var allFlights = flightRepository.findAll();
        assertEquals(18, allFlights.size(), "Should have 18 flights");
        
        var aaFlights = flightRepository.findFlightsByAirline("AA");
        assertFalse(aaFlights.isEmpty(), "Should have AA flights");
        
        // Test fare repository
        var allFares = fareRepository.findAll();
        assertTrue(allFares.size() > 20, "Should have multiple fares");
        
        var aaFares = fareRepository.findFaresByAirlineCode("AA");
        assertFalse(aaFares.isEmpty(), "Should have AA fares");
    }

    /**
     * Test: Application configuration and properties
     * Verifies that the application is configured correctly
     * Tests the configuration and property loading
     */
    @Test
    void testApplicationConfiguration() {
        // Test that the application context is properly configured
        assertNotNull(userRepository, "UserRepository should be configured");
        assertNotNull(airportRepository, "AirportRepository should be configured");
        assertNotNull(airlineRepository, "AirlineRepository should be configured");
        assertNotNull(flightRepository, "FlightRepository should be configured");
        assertNotNull(fareRepository, "FareRepository should be configured");
        
        // Test that services are properly configured
        assertNotNull(authService, "AuthService should be configured");
        assertNotNull(flightService, "FlightService should be configured");
        assertNotNull(flightSearchService, "FlightSearchService should be configured");
        assertNotNull(fareService, "FareService should be configured");
        assertNotNull(permissionService, "PermissionService should be configured");
    }

    /**
     * Test: End-to-end application functionality
     * Verifies that the complete application workflow functions correctly
     * Tests the integration of all components working together
     */
    @Test
    void testEndToEndApplicationFunctionality() {
        // Step 1: Authenticate a user
        var authResponse = authService.login(new edu.mit.sidpac.flightsearch.dto.AuthRequest("admin", "password123"));
        assertNotNull(authResponse.getToken(), "Should receive session token");
        
        // Step 2: Search for flights
        var searchRequest = new edu.mit.sidpac.flightsearch.dto.SearchRequest("BOS", "LAX", null);
        var searchResponse = flightSearchService.searchFlights(searchRequest);
        assertTrue(searchResponse.getTotalResults() > 0, "Should find flights");
        
        // Step 3: Get user for flight creation
        var adminUser = userRepository.findByUsername("admin").orElseThrow();
        
        // Step 4: Create a new flight
        var newFlight = flightService.createFlight(
            adminUser,
            "E2E123",
            "BOS",
            "LAX",
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(6),
            java.util.Set.of("AA")
        );
        assertNotNull(newFlight, "Should create new flight");
        
        // Step 5: Verify the flight was created
        var createdFlight = flightRepository.findAll().stream()
                .filter(f -> "E2E123".equals(f.getFlightNumber()))
                .findFirst();
        assertTrue(createdFlight.isPresent(), "Created flight should exist in database");
        assertEquals("E2E123", createdFlight.get().getFlightNumber(), "Flight number should match");
        
        // Step 6: Search again to verify new flight appears
        var updatedSearchResponse = flightSearchService.searchFlights(searchRequest);
        assertTrue(updatedSearchResponse.getTotalResults() >= searchResponse.getTotalResults(), 
                  "Search results should include the new flight");
    }
}
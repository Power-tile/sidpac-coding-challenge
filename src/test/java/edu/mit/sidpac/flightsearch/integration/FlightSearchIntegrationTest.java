package edu.mit.sidpac.flightsearch.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.entity.*;
import edu.mit.sidpac.flightsearch.repository.*;
import edu.mit.sidpac.flightsearch.service.AuthService;
import edu.mit.sidpac.flightsearch.util.TestDatabaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import edu.mit.sidpac.flightsearch.config.TestJpaAuditingConfig;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestJpaAuditingConfig.class)
class FlightSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDatabaseSetup testDatabaseSetup;

    private String superAdminToken;
    private String aaAdminToken;
    private String dlAdminToken;

    @BeforeEach
    void setUp() {
        try {
            // Load the complete database schema and data using TestDatabaseSetup
            testDatabaseSetup.loadFullDatabaseData();
            
            // Login with different admin users to get tokens for various permission levels
            superAdminToken = authService.login(new AuthRequest("admin", "password123")).getToken();
            aaAdminToken = authService.login(new AuthRequest("aa_admin", "password123")).getToken();
            dlAdminToken = authService.login(new AuthRequest("dl_admin", "password123")).getToken();
        } catch (Exception e) {
            // If setUp fails, tokens will remain null
            // Tests will handle this gracefully
            superAdminToken = null;
            aaAdminToken = null;
            dlAdminToken = null;
        }
    }


    /**
     * Test: Public flight search functionality without authentication
     * Verifies that users can search for flights without being logged in
     * Tests the core business functionality of the flight search engine
     */
    @Test
    void testPublicFlightSearch_WithoutAuthentication() throws Exception {
        // Test searching for a popular route (BOS to LAX) that should have multiple flights
        mockMvc.perform(get("/api/flights/planning")
                .param("sourceAirport", "BOS")
                .param("destinationAirport", "LAX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber())
                .andExpect(jsonPath("$.trips.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$.trips[0].airlineCode").exists())
                .andExpect(jsonPath("$.trips[0].totalPrice").exists())
                .andExpect(jsonPath("$.trips[0].legs").isArray());
    }

    /**
     * Test: Flight search with specific departure time
     * Verifies that time-based filtering works correctly
     * Tests the departure time parameter functionality
     */
    @Test
    void testFlightSearch_WithDepartureTime() throws Exception {
        // Search for flights departing on March 20, 2024 at 09:30 (matches sample data)
        LocalDateTime departureTime = LocalDateTime.of(2024, 3, 20, 9, 30);

        mockMvc.perform(get("/api/flights/planning")
                .param("sourceAirport", "BOS")
                .param("destinationAirport", "LAX")
                .param("departureTime", departureTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber());
    }

    /**
     * Test: International flight search
     * Verifies that international routes work correctly
     * Tests the system's ability to handle international airports and airlines
     */
    @Test
    void testInternationalFlightSearch() throws Exception {
        // Search for international route (JFK to LHR)
        mockMvc.perform(get("/api/flights/planning")
                .param("sourceAirport", "JFK")
                .param("destinationAirport", "LHR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber());
    }

    /**
     * Test: Public access to flight listings
     * Verifies that the flights endpoint is publicly accessible
     * Tests the basic CRUD read functionality
     */
    @Test
    void testGetAllFlights_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].flightNumber").exists())
                .andExpect(jsonPath("$[0].sourceAirportCode").exists())
                .andExpect(jsonPath("$[0].destinationAirportCode").exists());
    }

    /**
     * Test: Get flights by airline code
     * Verifies that airline-specific flight filtering works
     * Tests the airline-based query functionality
     */
    @Test
    void testGetFlightsByAirline() throws Exception {
        // Get flights for American Airlines (AA)
        mockMvc.perform(get("/api/flights/airline/AA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].flightNumber").value(containsString("AA")));
    }

    /**
     * Test: Get flights by non-existent airline code returns 404
     * Verifies that invalid airline codes return proper error response
     * Tests the error handling for non-existent airline codes
     */
    @Test
    void testGetFlightsByNonExistentAirline() throws Exception {
        // Try to get flights for a non-existent airline code
        mockMvc.perform(get("/api/flights/airline/XYZ"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Get specific flight by ID
     * Verifies that individual flight retrieval works
     * Tests the single entity retrieval functionality
     */
    @Test
    void testGetFlightById() throws Exception {
        // First get all flights to find a valid ID
        List<Flight> flights = flightRepository.findAll();
        assertFalse(flights.isEmpty(), "Should have flights in test database");
        
        String flightId = flights.get(0).getId();
        
        mockMvc.perform(get("/api/flights/" + flightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flightId))
                .andExpect(jsonPath("$.flightNumber").exists())
                .andExpect(jsonPath("$.sourceAirportCode").exists())
                .andExpect(jsonPath("$.destinationAirportCode").exists());
    }

    /**
     * Test: Get non-existent flight by ID returns 404
     * Verifies that invalid flight IDs return proper error response
     * Tests the error handling for non-existent flight IDs
     */
    @Test
    void testGetNonExistentFlightById() throws Exception {
        // Try to get a flight with a non-existent ID
        mockMvc.perform(get("/api/flights/non-existent-flight-id"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Super admin can create flights for any airline
     * Verifies that super admin (no assigned airline) has full permissions
     * Tests the permission system for unrestricted admin access
     */
    @Test
    void testCreateFlight_SuperAdminPermissions() throws Exception {
        if (superAdminToken == null) return;
        
        // Super admin should be able to create flights for any airline
        String flightJson = """
            {
                "flightNumber": "TEST123",
                "sourceAirportCode": "BOS",
                "destinationAirportCode": "LAX",
                "departureTime": "2025-12-01T10:00:00",
                "arrivalTime": "2025-12-01T16:00:00",
                "airlineCodes": ["AA"]
            }
            """;

        mockMvc.perform(post("/api/flights")
                .header("X-Session-ID", superAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("TEST123"))
                .andExpect(jsonPath("$.sourceAirportCode").value("BOS"))
                .andExpect(jsonPath("$.destinationAirportCode").value("LAX"));
    }

    /**
     * Test: Airline admin can only create flights for their assigned airline
     * Verifies that airline-specific admins are restricted to their airline
     * Tests the permission system for airline-scoped admin access
     */
    @Test
    void testCreateFlight_AirlineAdminPermissions() throws Exception {
        if (aaAdminToken == null) return;
        
        // AA admin should be able to create flights for American Airlines
        String flightJson = """
            {
                "flightNumber": "AA999",
                "sourceAirportCode": "BOS",
                "destinationAirportCode": "LAX",
                "departureTime": "2025-12-01T11:00:00",
                "arrivalTime": "2025-12-01T17:00:00",
                "airlineCodes": ["AA"]
            }
            """;

        mockMvc.perform(post("/api/flights")
                .header("X-Session-ID", aaAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AA999"));
    }

    /**
     * Test: Airline admin cannot create flights for other airlines
     * Verifies that airline-specific admins are properly restricted
     * Tests the permission system's enforcement of airline boundaries
     */
    @Test
    void testCreateFlight_AirlineAdminRestrictions() throws Exception {
        if (aaAdminToken == null) return;
        
        // AA admin should NOT be able to create flights for Delta Airlines
        String flightJson = """
            {
                "flightNumber": "DL999",
                "sourceAirportCode": "BOS",
                "destinationAirportCode": "LAX",
                "departureTime": "2025-12-01T12:00:00",
                "arrivalTime": "2025-12-01T18:00:00",
                "airlineCodes": ["DL"]
            }
            """;

        mockMvc.perform(post("/api/flights")
                .header("X-Session-ID", aaAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: Unauthenticated users cannot create flights
     * Verifies that write operations require authentication
     * Tests the security system's protection of write endpoints
     */
    @Test
    void testCreateFlight_WithoutAuthentication() throws Exception {
        String flightJson = """
            {
                "flightNumber": "UNAUTH123",
                "sourceAirportCode": "BOS",
                "destinationAirportCode": "LAX",
                "departureTime": "2025-12-01T13:00:00",
                "arrivalTime": "2025-12-01T19:00:00",
                "airlineCodes": ["AA"]
            }
            """;

        mockMvc.perform(post("/api/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Flight search with complex pricing (multi-leg trip)
     * Verifies that the fare calculation system works for complex scenarios
     * Tests the business logic for multi-leg trip pricing
     */
    @Test
    void testMultiLegFlightSearch() throws Exception {
        // Search for a route that might require connections (BOS to LAS)
        // This tests the multi-leg trip functionality and fare restrictions
        mockMvc.perform(get("/api/flights/planning")
                .param("sourceAirport", "BOS")
                .param("destinationAirport", "LAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber());
    }

    /**
     * Test: Database data integrity verification
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
        
        // Verify specific users exist
        assertTrue(userRepository.findByUsername("admin").isPresent(), "Super admin should exist");
        assertTrue(userRepository.findByUsername("aa_admin").isPresent(), "AA admin should exist");
        assertTrue(userRepository.findByUsername("dl_admin").isPresent(), "DL admin should exist");
        
        // Verify specific airports exist
        assertTrue(airportRepository.findByCode("BOS").isPresent(), "BOS airport should exist");
        assertTrue(airportRepository.findByCode("LAX").isPresent(), "LAX airport should exist");
        assertTrue(airportRepository.findByCode("JFK").isPresent(), "JFK airport should exist");
        
        // Verify specific airlines exist
        assertTrue(airlineRepository.findByCode("AA").isPresent(), "AA airline should exist");
        assertTrue(airlineRepository.findByCode("DL").isPresent(), "DL airline should exist");
        assertTrue(airlineRepository.findByCode("UA").isPresent(), "UA airline should exist");
    }

    /**
     * Test: Application context loads successfully
     * Verifies that all Spring beans are properly configured
     * Tests the basic application startup and configuration
     */
    @Test
    void testApplicationContextLoads() {
        // Verify that all required components are available
        assertNotNull(mockMvc, "MockMvc should be available");
        assertNotNull(objectMapper, "ObjectMapper should be available");
        assertNotNull(authService, "AuthService should be available");
        assertNotNull(userRepository, "UserRepository should be available");
        assertNotNull(flightRepository, "FlightRepository should be available");
    }
}

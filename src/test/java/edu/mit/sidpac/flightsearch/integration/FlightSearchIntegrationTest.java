package edu.mit.sidpac.flightsearch.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.SearchRequest;
import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Airport;
import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import edu.mit.sidpac.flightsearch.repository.AirlineRepository;
import edu.mit.sidpac.flightsearch.repository.AirportRepository;
import edu.mit.sidpac.flightsearch.repository.FlightRepository;
import edu.mit.sidpac.flightsearch.repository.UserRepository;
import edu.mit.sidpac.flightsearch.service.AuthService;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
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
    private PasswordEncoder passwordEncoder;

    private String authToken;

    @BeforeEach
    void setUp() {
        // Create test data
        createTestData();
        
        // Login and get token
        AuthRequest loginRequest = new AuthRequest("testuser", "password123");
        authToken = authService.login(loginRequest).getToken();
    }

    private void createTestData() {
        // Create test user
        User user = new User("testuser", "test@example.com", 
                           passwordEncoder.encode("password123"), 
                           "Test", "User", UserRole.ADMIN);
        userRepository.save(user);

        // Create test airports
        Airport bos = new Airport("BOS", "Logan International", "Boston", "USA");
        Airport lax = new Airport("LAX", "Los Angeles International", "Los Angeles", "USA");
        airportRepository.save(bos);
        airportRepository.save(lax);

        // Create test airline
        Airline aa = new Airline("AA", "American Airlines", "USA");
        airlineRepository.save(aa);

        // Create test flight
        Flight flight = new Flight("AA123", bos, lax, 
                                 LocalDateTime.now().plusHours(1), 
                                 LocalDateTime.now().plusHours(6));
        flightRepository.save(flight);
    }

    @Test
    void testSearchFlights_WithAuthentication() throws Exception {
        SearchRequest request = new SearchRequest("BOS", "LAX", LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/search/flights")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber());
    }

    @Test
    void testSearchFlights_WithoutAuthentication() throws Exception {
        SearchRequest request = new SearchRequest("BOS", "LAX", LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/search/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Search endpoint is public
    }

    @Test
    void testGetFlights_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetFlights_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk());
    }
}

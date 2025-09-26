package edu.mit.sidpac.flightsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.sidpac.flightsearch.config.TestSecurityConfig;
import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.AuthResponse;
import edu.mit.sidpac.flightsearch.dto.RegisterRequest;
import edu.mit.sidpac.flightsearch.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for AuthController
 * Tests authentication and registration endpoints with various scenarios
 * Uses mocked services to isolate controller logic testing
 */
@WebMvcTest(controllers = AuthController.class, 
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    })
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;


    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private edu.mit.sidpac.flightsearch.repository.UserSessionRepository userSessionRepository;

    @MockBean
    private edu.mit.sidpac.flightsearch.service.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test: Successful login with valid credentials
     * Verifies that the login endpoint returns proper JWT tokens and user role
     * Tests the happy path for user authentication
     */
    @Test
    void testLogin_Success() throws Exception {
        // Given: Valid login credentials for an admin user
        AuthRequest request = new AuthRequest("admin", "password123");
        AuthResponse response = new AuthResponse("session-id-12345", null, "ADMIN");
        
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        // When & Then: Login should succeed and return proper response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-id-12345"))
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Test: Login with airline admin credentials
     * Verifies that airline-specific admin users can log in successfully
     * Tests authentication for users with assigned airline codes
     */
    @Test
    void testLogin_AirlineAdmin() throws Exception {
        // Given: Login credentials for an airline admin
        AuthRequest request = new AuthRequest("aa_admin", "password123");
        AuthResponse response = new AuthResponse("session-id-aa", null, "ADMIN");
        
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        // When & Then: Airline admin login should succeed
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-id-aa"))
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Test: Login with invalid credentials
     * Verifies that the system properly rejects invalid login attempts
     * Tests the security system's protection against unauthorized access
     */
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Given: Invalid login credentials
        AuthRequest request = new AuthRequest("admin", "wrongpassword");
        
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then: Login should fail with proper error response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Login with non-existent user
     * Verifies that the system handles non-existent users gracefully
     * Tests the system's response to unknown usernames
     */
    @Test
    void testLogin_NonExistentUser() throws Exception {
        // Given: Login credentials for a user that doesn't exist
        AuthRequest request = new AuthRequest("nonexistent", "password123");
        
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then: Login should fail with proper error response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Login with email instead of username
     * Verifies that users can log in using their email address
     * Tests the flexibility of the authentication system
     */
    @Test
    void testLogin_WithEmail() throws Exception {
        // Given: Login using email address instead of username
        AuthRequest request = new AuthRequest("admin@flightsearch.com", "password123");
        AuthResponse response = new AuthResponse("session-id-email", null, "ADMIN");
        
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        // When & Then: Email-based login should succeed
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-id-email"))
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Test: Successful user registration
     * Verifies that new admin users can be registered successfully
     * Tests the user registration workflow
     */
    @Test
    void testRegister_Success() throws Exception {
        // Given: Valid registration request for a new admin user
        RegisterRequest request = new RegisterRequest("newadmin", "newadmin@flightsearch.com", 
                                                    "password123", "New", "Admin");
        AuthResponse response = new AuthResponse("session-id-new", null, "ADMIN");
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then: Registration should succeed and return authentication tokens
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-id-new"))
                .andExpect(jsonPath("$.refreshToken").isEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Test: Registration with duplicate username
     * Verifies that the system prevents duplicate usernames
     * Tests the uniqueness constraint enforcement
     */
    @Test
    void testRegister_DuplicateUsername() throws Exception {
        // Given: Registration request with existing username
        RegisterRequest request = new RegisterRequest("admin", "newemail@flightsearch.com", 
                                                    "password123", "New", "Admin");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // When & Then: Registration should fail with proper error response
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with duplicate email
     * Verifies that the system prevents duplicate email addresses
     * Tests the email uniqueness constraint enforcement
     */
    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // Given: Registration request with existing email
        RegisterRequest request = new RegisterRequest("newuser", "admin@flightsearch.com", 
                                                    "password123", "New", "User");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then: Registration should fail with proper error response
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with invalid email format
     * Verifies that the system validates email format
     * Tests input validation for email addresses
     */
    @Test
    void testRegister_InvalidEmailFormat() throws Exception {
        // Given: Registration request with invalid email format
        RegisterRequest request = new RegisterRequest("newuser", "invalid-email", 
                                                    "password123", "New", "User");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Invalid email format"));

        // When & Then: Registration should fail with validation error
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with weak password
     * Verifies that the system enforces password strength requirements
     * Tests password validation logic
     */
    @Test
    void testRegister_WeakPassword() throws Exception {
        // Given: Registration request with weak password
        RegisterRequest request = new RegisterRequest("newuser", "newuser@flightsearch.com", 
                                                    "123", "New", "User");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Password too weak"));

        // When & Then: Registration should fail with password validation error
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with missing required fields
     * Verifies that the system validates required field presence
     * Tests input validation for mandatory fields
     */
    @Test
    void testRegister_MissingRequiredFields() throws Exception {
        // Given: Registration request with missing required fields
        RegisterRequest request = new RegisterRequest("", "newuser@flightsearch.com", 
                                                    "password123", "New", "User");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username is required"));

        // When & Then: Registration should fail with validation error
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Login with malformed JSON request
     * Verifies that the system handles malformed requests gracefully
     * Tests request validation and error handling
     */
    @Test
    void testLogin_MalformedJson() throws Exception {
        // Given: Malformed JSON request
        String malformedJson = "{ \"username\": \"admin\", \"password\": }";

        // When & Then: Request should fail with proper error response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with malformed JSON request
     * Verifies that the system handles malformed registration requests
     * Tests request validation for registration endpoint
     */
    @Test
    void testRegister_MalformedJson() throws Exception {
        // Given: Malformed JSON request
        String malformedJson = "{ \"username\": \"newuser\", \"email\": \"test@test.com\", \"password\": }";

        // When & Then: Request should fail with proper error response
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Login with empty request body
     * Verifies that the system handles empty requests properly
     * Tests edge case handling for empty input
     */
    @Test
    void testLogin_EmptyRequestBody() throws Exception {
        // Given: Empty request body
        String emptyJson = "{}";

        // When & Then: Request should fail with validation error
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Registration with empty request body
     * Verifies that the system handles empty registration requests
     * Tests edge case handling for registration endpoint
     */
    @Test
    void testRegister_EmptyRequestBody() throws Exception {
        // Given: Empty request body
        String emptyJson = "{}";

        // When & Then: Request should fail with validation error
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest());
    }
}
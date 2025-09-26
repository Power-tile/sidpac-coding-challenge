package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.*;
import edu.mit.sidpac.flightsearch.repository.*;
import edu.mit.sidpac.flightsearch.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for FlightService
 * Tests the business logic for flight management operations
 * Verifies permission checks, data validation, and error handling
 * Uses mocked dependencies to isolate service layer testing
 */
@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AirlineRepository airlineRepository;


    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private FlightService flightService;

    private User superAdmin;
    private User aaAdmin;
    private User dlAdmin;
    private Airport sourceAirport;
    private Airport destinationAirport;
    private Airline americanAirlines;
    private Airline deltaAirlines;
    private Flight existingFlight;

    @BeforeEach
    void setUp() {
        // Create test users with different permission levels
        superAdmin = new User("admin", "admin@flightsearch.com", "hashedPassword", 
                           "Super", "Admin", UserRole.ADMIN);
        superAdmin.setAssignedAirlineCode(null); // Super admin has no airline restriction
        
        aaAdmin = new User("aa_admin", "aa_admin@flightsearch.com", "hashedPassword", 
                         "American", "Admin", UserRole.ADMIN);
        aaAdmin.setAssignedAirlineCode("AA"); // AA admin restricted to American Airlines
        
        dlAdmin = new User("dl_admin", "dl_admin@flightsearch.com", "hashedPassword", 
                         "Delta", "Admin", UserRole.ADMIN);
        dlAdmin.setAssignedAirlineCode("DL"); // DL admin restricted to Delta Airlines
        
        // Create test airports
        sourceAirport = new Airport("BOS", "Logan International Airport", "Boston", "USA");
        destinationAirport = new Airport("LAX", "Los Angeles International Airport", "Los Angeles", "USA");
        
        // Create test airlines
        americanAirlines = new Airline("AA", "American Airlines", "USA");
        deltaAirlines = new Airline("DL", "Delta Air Lines", "USA");
        
        // Create existing flight for update/delete tests
        existingFlight = new Flight("AA123", sourceAirport, destinationAirport, 
                                  LocalDateTime.now().plusHours(1), 
                                  LocalDateTime.now().plusHours(6));
        existingFlight.setId("flight-aa123-001");
        
        // Add American Airlines to the existing flight
        FlightAirline flightAirline = new FlightAirline(existingFlight, americanAirlines);
        existingFlight.getFlightAirlines().add(flightAirline);
    }

    /**
     * Test: Super admin can create flights for any airline
     * Verifies that super admin (no assigned airline) has unrestricted permissions
     * Tests the permission system for unrestricted admin access
     */
    @Test
    void testCreateFlight_SuperAdminCanCreateForAnyAirline() {
        // Given: Super admin trying to create a flight for American Airlines
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        when(airlineRepository.findByCode("AA")).thenReturn(Optional.of(americanAirlines));
        when(permissionService.canManageFlights(superAdmin, "AA")).thenReturn(true);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Super admin creates a flight for American Airlines
        Flight result = flightService.createFlight(
                superAdmin,
                "AA999",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
        );

        // Then: Flight should be created successfully
        assertNotNull(result);
        assertEquals("AA999", result.getFlightNumber());
        assertEquals(sourceAirport, result.getSourceAirport());
        assertEquals(destinationAirport, result.getDestinationAirport());
        verify(flightRepository, times(2)).save(any(Flight.class));
    }

    /**
     * Test: Airline admin can create flights for their assigned airline
     * Verifies that airline-specific admins can manage their airline's flights
     * Tests the permission system for airline-scoped admin access
     */
    @Test
    void testCreateFlight_AirlineAdminCanCreateForAssignedAirline() {
        // Given: AA admin trying to create a flight for American Airlines
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        when(airlineRepository.findByCode("AA")).thenReturn(Optional.of(americanAirlines));
        when(permissionService.canManageFlights(aaAdmin, "AA")).thenReturn(true);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: AA admin creates a flight for American Airlines
        Flight result = flightService.createFlight(
                aaAdmin,
                "AA888",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
        );

        // Then: Flight should be created successfully
        assertNotNull(result);
        assertEquals("AA888", result.getFlightNumber());
        verify(flightRepository, times(2)).save(any(Flight.class));
    }

    /**
     * Test: Airline admin cannot create flights for other airlines
     * Verifies that airline-specific admins are restricted to their assigned airline
     * Tests the permission system's enforcement of airline boundaries
     */
    @Test
    void testCreateFlight_AirlineAdminCannotCreateForOtherAirlines() {
        // Given: AA admin trying to create a flight for Delta Airlines
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        // when(airlineRepository.findByCode("DL")).thenReturn(Optional.of(deltaAirlines));
        when(permissionService.canManageFlights(aaAdmin, "DL")).thenReturn(false);

        // When & Then: AA admin should not be able to create flights for Delta Airlines
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                aaAdmin,
                "DL777",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("DL")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with codeshare airlines
     * Verifies that flights can be created with multiple airlines (codeshare)
     * Tests the codeshare functionality and multi-airline flight creation
     */
    @Test
    void testCreateFlight_WithCodeshareAirlines() {
        // Given: Creating a flight with multiple airlines (codeshare)
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        when(airlineRepository.findByCode("AA")).thenReturn(Optional.of(americanAirlines));
        when(airlineRepository.findByCode("DL")).thenReturn(Optional.of(deltaAirlines));
        when(permissionService.canManageFlights(superAdmin, "AA")).thenReturn(true);
        when(permissionService.canManageFlights(superAdmin, "DL")).thenReturn(true);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Creating a flight with codeshare airlines
        Flight result = flightService.createFlight(
                superAdmin,
                "CODESHARE123",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA", "DL")
        );

        // Then: Flight should be created with multiple airline relationships
        assertNotNull(result);
        assertEquals("CODESHARE123", result.getFlightNumber());
        verify(flightRepository, times(2)).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with invalid source airport
     * Verifies that the system validates airport existence
     * Tests the data validation for airport codes
     */
    @Test
    void testCreateFlight_InvalidSourceAirport() {
        // Given: Invalid source airport code
        when(airportRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When & Then: Flight creation should fail with airport not found error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "INVALID123",
                "INVALID",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with invalid destination airport
     * Verifies that the system validates destination airport existence
     * Tests the data validation for destination airport codes
     */
    @Test
    void testCreateFlight_InvalidDestinationAirport() {
        // Given: Valid source but invalid destination airport
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When & Then: Flight creation should fail with destination airport not found error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "INVALID123",
                "BOS",
                "INVALID",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with invalid airline code
     * Verifies that the system validates airline existence
     * Tests the data validation for airline codes
     */
    @Test
    void testCreateFlight_InvalidAirlineCode() {
        // Given: Valid airports but invalid airline code
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        // when(airlineRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When & Then: Flight creation should fail with airline not found error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "INVALID123",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("INVALID")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with invalid departure time
     * Verifies that the system validates departure time logic
     * Tests the business logic for time validation
     */
    @Test
    void testCreateFlight_InvalidDepartureTime() {
        // Given: Departure time in the past
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        // When & Then: Flight creation should fail with invalid time error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "PAST123",
                "BOS",
                "LAX",
                pastTime,
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with invalid arrival time
     * Verifies that the system validates arrival time logic
     * Tests the business logic for arrival time validation
     */
    @Test
    void testCreateFlight_InvalidArrivalTime() {
        // Given: Arrival time before departure time
        LocalDateTime departureTime = LocalDateTime.now().plusHours(1);
        LocalDateTime arrivalTime = LocalDateTime.now().plusMinutes(30); // Before departure

        // When & Then: Flight creation should fail with invalid time error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "INVALID123",
                "BOS",
                "LAX",
                departureTime,
                arrivalTime,
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight creation with empty airline codes
     * Verifies that the system validates airline codes presence
     * Tests the business logic for required airline codes
     */
    @Test
    void testCreateFlight_EmptyAirlineCodes() {
        // When & Then: Flight creation should fail with empty airline codes error
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                superAdmin,
                "EMPTY123",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of() // Empty airline codes
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight update with valid permissions
     * Verifies that authorized users can update flights
     * Tests the flight update functionality
     */
    @Test
    void testUpdateFlight_WithValidPermissions() {
        // Given: Super admin updating an existing flight
        when(flightRepository.findById("flight-aa123-001")).thenReturn(Optional.of(existingFlight));
        when(airportRepository.findByCode("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCode("LAX")).thenReturn(Optional.of(destinationAirport));
        when(airlineRepository.findByCode("AA")).thenReturn(Optional.of(americanAirlines));
        when(permissionService.canManageFlights(superAdmin, "AA")).thenReturn(true);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Updating the flight
        Flight updatedFlight = flightService.updateFlight(
                superAdmin,
                "flight-aa123-001",
                "AA456", // New flight number
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(7),
                Set.of("AA")
        );

        // Then: Flight should be updated successfully
        assertNotNull(updatedFlight);
        assertEquals("AA456", updatedFlight.getFlightNumber());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    /**
     * Test: Flight update with insufficient permissions
     * Verifies that unauthorized users cannot update flights
     * Tests the permission system for flight updates
     */
    @Test
    void testUpdateFlight_WithInsufficientPermissions() {
        // Given: DL admin trying to update an AA flight
        when(flightRepository.findById("flight-aa123-001")).thenReturn(Optional.of(existingFlight));

        // When & Then: Flight update should fail with permission error
        assertThrows(RuntimeException.class, () -> {
            flightService.updateFlight(
                dlAdmin,
                "flight-aa123-001",
                "AA456",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(7),
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight update with non-existent flight
     * Verifies that the system handles non-existent flight updates
     * Tests the error handling for missing flights
     */
    @Test
    void testUpdateFlight_NonExistentFlight() {
        // Given: Non-existent flight ID
        when(flightRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then: Flight update should fail with flight not found error
        assertThrows(RuntimeException.class, () -> {
            flightService.updateFlight(
                superAdmin,
                "non-existent",
                "AA456",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(7),
                Set.of("AA")
            );
        });

        // Verify that no flight was saved
        verify(flightRepository, never()).save(any(Flight.class));
    }

    /**
     * Test: Flight deletion with valid permissions
     * Verifies that authorized users can delete flights
     * Tests the flight deletion functionality
     */
    @Test
    void testDeleteFlight_WithValidPermissions() {
        // Given: Super admin deleting an existing flight
        when(flightRepository.findById("flight-aa123-001")).thenReturn(Optional.of(existingFlight));
        when(permissionService.canManageFlights(superAdmin, "AA")).thenReturn(true);

        // When: Deleting the flight
        flightService.deleteFlight(superAdmin, "flight-aa123-001");

        // Then: Flight should be deleted successfully
        verify(flightRepository, times(1)).delete(existingFlight);
    }

    /**
     * Test: Flight deletion with insufficient permissions
     * Verifies that unauthorized users cannot delete flights
     * Tests the permission system for flight deletion
     */
    @Test
    void testDeleteFlight_WithInsufficientPermissions() {
        // Given: DL admin trying to delete an AA flight
        when(flightRepository.findById("flight-aa123-001")).thenReturn(Optional.of(existingFlight));
        when(permissionService.canManageFlights(dlAdmin, "AA")).thenReturn(false);

        // When & Then: Flight deletion should fail with permission error
        assertThrows(RuntimeException.class, () -> {
            flightService.deleteFlight(dlAdmin, "flight-aa123-001");
        });

        // Verify that no flight was deleted
        verify(flightRepository, never()).delete(any(Flight.class));
    }

    /**
     * Test: Flight deletion with non-existent flight
     * Verifies that the system handles non-existent flight deletion
     * Tests the error handling for missing flights
     */
    @Test
    void testDeleteFlight_NonExistentFlight() {
        // Given: Non-existent flight ID
        when(flightRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then: Flight deletion should fail with flight not found error
        assertThrows(RuntimeException.class, () -> {
            flightService.deleteFlight(superAdmin, "non-existent");
        });

        // Verify that no flight was deleted
        verify(flightRepository, never()).delete(any(Flight.class));
    }
}
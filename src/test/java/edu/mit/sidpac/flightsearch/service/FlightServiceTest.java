package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Airport;
import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import edu.mit.sidpac.flightsearch.repository.AirlineRepository;
import edu.mit.sidpac.flightsearch.repository.AirportRepository;
import edu.mit.sidpac.flightsearch.repository.FlightRepository;
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

    private User adminUser;
    private Airport sourceAirport;
    private Airport destinationAirport;
    private Airline airline;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "admin@test.com", "hashedPassword", 
                           "Admin", "User", UserRole.ADMIN);
        
        sourceAirport = new Airport("BOS", "Logan International", "Boston", "USA");
        destinationAirport = new Airport("LAX", "Los Angeles International", "Los Angeles", "USA");
        airline = new Airline("AA", "American Airlines", "USA");
    }

    @Test
    void testCreateFlight_Success() {
        // Given
        when(airportRepository.findByCodeAndIsActiveTrue("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCodeAndIsActiveTrue("LAX")).thenReturn(Optional.of(destinationAirport));
        when(airlineRepository.findByCodeAndIsActiveTrue("AA")).thenReturn(Optional.of(airline));
        when(permissionService.canManageFlights(any(User.class), anyString())).thenReturn(true);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Flight result = flightService.createFlight(
                adminUser,
                "AA123",
                "BOS",
                "LAX",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(6),
                Set.of("AA")
        );

        // Then
        assertNotNull(result);
        assertEquals("AA123", result.getFlightNumber());
        assertEquals(sourceAirport, result.getSourceAirport());
        assertEquals(destinationAirport, result.getDestinationAirport());
        verify(flightRepository, times(2)).save(any(Flight.class));
    }

    @Test
    void testCreateFlight_InsufficientPermissions() {
        // Given
        when(airportRepository.findByCodeAndIsActiveTrue("BOS")).thenReturn(Optional.of(sourceAirport));
        when(airportRepository.findByCodeAndIsActiveTrue("LAX")).thenReturn(Optional.of(destinationAirport));
        when(permissionService.canManageFlights(any(User.class), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                    adminUser,
                    "AA123",
                    "BOS",
                    "LAX",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(6),
                    Set.of("AA")
            );
        });
    }

    @Test
    void testCreateFlight_AirportNotFound() {
        // Given
        when(airportRepository.findByCodeAndIsActiveTrue("BOS")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            flightService.createFlight(
                    adminUser,
                    "AA123",
                    "BOS",
                    "LAX",
                    LocalDateTime.now().plusHours(1),
                    LocalDateTime.now().plusHours(6),
                    Set.of("AA")
            );
        });
    }
}

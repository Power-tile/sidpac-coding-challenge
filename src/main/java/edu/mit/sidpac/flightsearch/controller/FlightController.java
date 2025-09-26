package edu.mit.sidpac.flightsearch.controller;

import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/flights")
@Tag(name = "Flights", description = "Flight management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @GetMapping
    @Operation(summary = "Get all flights", description = "Retrieve all active flights with optional pagination")
    public ResponseEntity<?> getAllFlights(Pageable pageable) {
        if (pageable.getPageSize() > 0) {
            Page<Flight> flights = flightService.getAllFlights(pageable);
            return ResponseEntity.ok(flights);
        } else {
            List<Flight> flights = flightService.getAllFlights();
            return ResponseEntity.ok(flights);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Retrieve a specific flight by its ID")
    public ResponseEntity<Flight> getFlightById(@PathVariable String id) {
        Optional<Flight> flight = flightService.getFlightById(id);
        return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create flight", description = "Create a new flight (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> createFlight(@RequestBody CreateFlightRequest request) {
        try {
            // Get current user from security context
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            // Note: In a real implementation, you'd inject the AuthService to get the full user object
            // For now, we'll create a mock user - this should be improved in production
            User currentUser = new User();
            currentUser.setRole(edu.mit.sidpac.flightsearch.entity.UserRole.ADMIN);
            
            Flight flight = flightService.createFlight(
                    currentUser,
                    request.getFlightNumber(),
                    request.getSourceAirportCode(),
                    request.getDestinationAirportCode(),
                    request.getDepartureTime(),
                    request.getArrivalTime(),
                    request.getAirlineCodes()
            );
            return ResponseEntity.ok(flight);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update flight", description = "Update an existing flight (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> updateFlight(@PathVariable String id, @RequestBody UpdateFlightRequest request) {
        try {
            Flight flight = flightService.updateFlight(
                    id,
                    request.getFlightNumber(),
                    request.getSourceAirportCode(),
                    request.getDestinationAirportCode(),
                    request.getDepartureTime(),
                    request.getArrivalTime(),
                    request.getAirlineCodes()
            );
            return ResponseEntity.ok(flight);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete flight", description = "Delete a flight (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable String id) {
        try {
            flightService.deleteFlight(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Search for flights between airports")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) LocalDateTime departureTime) {
        
        List<Flight> flights;
        if (departureTime != null) {
            flights = flightService.searchFlights(source, destination, departureTime);
        } else {
            flights = flightService.searchFlights(source, destination);
        }
        
        return ResponseEntity.ok(flights);
    }
    
    @GetMapping("/airline/{airlineCode}")
    @Operation(summary = "Get flights by airline", description = "Get all flights for a specific airline")
    public ResponseEntity<List<Flight>> getFlightsByAirline(@PathVariable String airlineCode) {
        List<Flight> flights = flightService.getFlightsByAirline(airlineCode);
        return ResponseEntity.ok(flights);
    }
    
    // DTOs for request bodies
    public static class CreateFlightRequest {
        private String flightNumber;
        private String sourceAirportCode;
        private String destinationAirportCode;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private Set<String> airlineCodes;
        
        // Getters and Setters
        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
        
        public String getSourceAirportCode() { return sourceAirportCode; }
        public void setSourceAirportCode(String sourceAirportCode) { this.sourceAirportCode = sourceAirportCode; }
        
        public String getDestinationAirportCode() { return destinationAirportCode; }
        public void setDestinationAirportCode(String destinationAirportCode) { this.destinationAirportCode = destinationAirportCode; }
        
        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
        
        public LocalDateTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
        
        public Set<String> getAirlineCodes() { return airlineCodes; }
        public void setAirlineCodes(Set<String> airlineCodes) { this.airlineCodes = airlineCodes; }
    }
    
    public static class UpdateFlightRequest extends CreateFlightRequest {
        // Same fields as CreateFlightRequest
    }
}

package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Airport;
import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.FlightAirline;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.exception.InsufficientPermissionsException;
import edu.mit.sidpac.flightsearch.repository.AirlineRepository;
import edu.mit.sidpac.flightsearch.repository.AirportRepository;
import edu.mit.sidpac.flightsearch.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private AirlineRepository airlineRepository;
    
    @Autowired
    private PermissionService permissionService;
    
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
    
    public Page<Flight> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }
    
    public Optional<Flight> getFlightById(String id) {
        return flightRepository.findById(id);
    }
    
    public Flight createFlight(User user, String flightNumber, String sourceAirportCode, String destinationAirportCode,
                              LocalDateTime departureTime, LocalDateTime arrivalTime, Set<String> airlineCodes) {
        
        // Validate airline codes
        if (airlineCodes == null || airlineCodes.isEmpty()) {
            throw new RuntimeException("Airline codes cannot be empty");
        }
        
        // Validate departure time
        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Departure time cannot be in the past");
        }
        
        // Validate arrival time
        if (arrivalTime.isBefore(departureTime)) {
            throw new RuntimeException("Arrival time cannot be before departure time");
        }
        
        Airport sourceAirport = airportRepository.findByCode(sourceAirportCode)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceAirportCode));
        
        Airport destinationAirport = airportRepository.findByCode(destinationAirportCode)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destinationAirportCode));
        
        // Validate airline codes and permissions before creating the flight
        for (String airlineCode : airlineCodes) {
            if (!permissionService.canManageFlights(user, airlineCode)) {
                throw new InsufficientPermissionsException("Insufficient permissions to manage airline: " + airlineCode);
            }
            
            Airline airline = airlineRepository.findByCode(airlineCode)
                    .orElseThrow(() -> new RuntimeException("Airline not found: " + airlineCode));
        }
        
        Flight flight = new Flight(flightNumber, sourceAirport, destinationAirport, departureTime, arrivalTime);
        flight = flightRepository.save(flight);
        
        // Add airline codeshares (validation already done above)
        for (String airlineCode : airlineCodes) {
            Airline airline = airlineRepository.findByCode(airlineCode).orElseThrow();
            FlightAirline flightAirline = new FlightAirline(flight, airline);
            flight.getFlightAirlines().add(flightAirline);
        }
        
        return flightRepository.save(flight);
    }
    
    public Flight updateFlight(User user, String id, String flightNumber, String sourceAirportCode, 
                              String destinationAirportCode, LocalDateTime departureTime, 
                              LocalDateTime arrivalTime, Set<String> airlineCodes) {
        
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + id));
        
        // Check permissions for all airlines associated with this flight
        for (FlightAirline flightAirline : flight.getFlightAirlines()) {
            if (!permissionService.canManageFlights(user, flightAirline.getAirline().getCode())) {
                throw new InsufficientPermissionsException("Insufficient permissions to update this flight");
            }
        }
        
        // Validate airline codes
        if (airlineCodes == null || airlineCodes.isEmpty()) {
            throw new RuntimeException("Airline codes cannot be empty");
        }
        
        // Validate departure time
        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Departure time cannot be in the past");
        }
        
        // Validate arrival time
        if (arrivalTime.isBefore(departureTime)) {
            throw new RuntimeException("Arrival time cannot be before departure time");
        }
        
        Airport sourceAirport = airportRepository.findByCode(sourceAirportCode)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceAirportCode));
        
        Airport destinationAirport = airportRepository.findByCode(destinationAirportCode)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destinationAirportCode));
        
        flight.setFlightNumber(flightNumber);
        flight.setSourceAirport(sourceAirport);
        flight.setDestinationAirport(destinationAirport);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        
        // Update airline codeshares with permission check
        flight.getFlightAirlines().clear();
        for (String airlineCode : airlineCodes) {
            if (!permissionService.canManageFlights(user, airlineCode)) {
                throw new InsufficientPermissionsException("Insufficient permissions to manage airline: " + airlineCode);
            }
            
            Airline airline = airlineRepository.findByCode(airlineCode)
                    .orElseThrow(() -> new RuntimeException("Airline not found: " + airlineCode));
            
            FlightAirline flightAirline = new FlightAirline(flight, airline);
            flight.getFlightAirlines().add(flightAirline);
        }
        
        return flightRepository.save(flight);
    }
    
    public void deleteFlight(User user, String id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + id));
        
        // Check permissions for all airlines associated with this flight
        for (FlightAirline flightAirline : flight.getFlightAirlines()) {
            if (!permissionService.canManageFlights(user, flightAirline.getAirline().getCode())) {
                throw new InsufficientPermissionsException("Insufficient permissions to delete this flight");
            }
        }
        
        flightRepository.delete(flight);
    }
    
    public List<Flight> searchFlights(String sourceCode, String destinationCode) {
        return flightRepository.findDirectFlights(sourceCode, destinationCode);
    }
    
    public List<Flight> searchFlights(String sourceCode, String destinationCode, LocalDateTime departureTime) {
        return flightRepository.findFlightsFromAirportAfterTime(sourceCode, departureTime)
                .stream()
                .filter(flight -> flight.getDestinationAirport().getCode().equals(destinationCode))
                .toList();
    }
    
    public List<Flight> getFlightsByAirline(String airlineCode) {
        return flightRepository.findFlightsByAirline(airlineCode);
    }
    
    public boolean airlineExists(String airlineCode) {
        return airlineRepository.existsByCode(airlineCode);
    }
}

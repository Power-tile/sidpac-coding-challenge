package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Airport;
import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.FlightAirline;
import edu.mit.sidpac.flightsearch.entity.User;
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
        return flightRepository.findByIsActiveTrue();
    }
    
    public Page<Flight> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }
    
    public Optional<Flight> getFlightById(String id) {
        return flightRepository.findById(id).filter(Flight::getIsActive);
    }
    
    public Flight createFlight(User user, String flightNumber, String sourceAirportCode, String destinationAirportCode,
                              LocalDateTime departureTime, LocalDateTime arrivalTime, Set<String> airlineCodes) {
        
        Airport sourceAirport = airportRepository.findByCodeAndIsActiveTrue(sourceAirportCode)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceAirportCode));
        
        Airport destinationAirport = airportRepository.findByCodeAndIsActiveTrue(destinationAirportCode)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destinationAirportCode));
        
        Flight flight = new Flight(flightNumber, sourceAirport, destinationAirport, departureTime, arrivalTime);
        flight = flightRepository.save(flight);
        
        // Add airline codeshares with permission check
        for (String airlineCode : airlineCodes) {
            if (!permissionService.canManageFlights(user, airlineCode)) {
                throw new RuntimeException("Insufficient permissions to manage airline: " + airlineCode);
            }
            
            Airline airline = airlineRepository.findByCodeAndIsActiveTrue(airlineCode)
                    .orElseThrow(() -> new RuntimeException("Airline not found: " + airlineCode));
            
            FlightAirline flightAirline = new FlightAirline(flight, airline);
            flight.getFlightAirlines().add(flightAirline);
        }
        
        return flightRepository.save(flight);
    }
    
    public Flight updateFlight(String id, String flightNumber, String sourceAirportCode, 
                              String destinationAirportCode, LocalDateTime departureTime, 
                              LocalDateTime arrivalTime, Set<String> airlineCodes) {
        
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + id));
        
        if (!flight.getIsActive()) {
            throw new RuntimeException("Flight not found: " + id);
        }
        
        Airport sourceAirport = airportRepository.findByCodeAndIsActiveTrue(sourceAirportCode)
                .orElseThrow(() -> new RuntimeException("Source airport not found: " + sourceAirportCode));
        
        Airport destinationAirport = airportRepository.findByCodeAndIsActiveTrue(destinationAirportCode)
                .orElseThrow(() -> new RuntimeException("Destination airport not found: " + destinationAirportCode));
        
        flight.setFlightNumber(flightNumber);
        flight.setSourceAirport(sourceAirport);
        flight.setDestinationAirport(destinationAirport);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        
        // Update airline codeshares
        flight.getFlightAirlines().clear();
        for (String airlineCode : airlineCodes) {
            Airline airline = airlineRepository.findByCodeAndIsActiveTrue(airlineCode)
                    .orElseThrow(() -> new RuntimeException("Airline not found: " + airlineCode));
            
            FlightAirline flightAirline = new FlightAirline(flight, airline);
            flight.getFlightAirlines().add(flightAirline);
        }
        
        return flightRepository.save(flight);
    }
    
    public void deleteFlight(String id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + id));
        
        flight.setIsActive(false);
        flightRepository.save(flight);
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
}

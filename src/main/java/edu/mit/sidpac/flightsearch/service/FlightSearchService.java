package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.dto.SearchRequest;
import edu.mit.sidpac.flightsearch.dto.SearchResponse;
import edu.mit.sidpac.flightsearch.dto.Trip;
import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Fare;
import edu.mit.sidpac.flightsearch.entity.FareRestriction;
import edu.mit.sidpac.flightsearch.entity.Flight;
import edu.mit.sidpac.flightsearch.entity.RestrictionType;
import edu.mit.sidpac.flightsearch.repository.FareRepository;
import edu.mit.sidpac.flightsearch.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlightSearchService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private FareRepository fareRepository;
    
    public SearchResponse searchFlights(SearchRequest request) {
        String sourceCode = request.getSourceAirport().toUpperCase();
        String destinationCode = request.getDestinationAirport().toUpperCase();
        LocalDateTime departureTime = request.getDepartureTime();
        
        // Find all possible flights
        List<Flight> allFlights = flightRepository.findByIsActiveTrue();
        
        // Find direct flights
        List<Trip> directTrips = findDirectTrips(allFlights, sourceCode, destinationCode, departureTime);
        
        // Find connecting flights
        List<Trip> connectingTrips = findConnectingTrips(allFlights, sourceCode, destinationCode, departureTime);
        
        // Combine and sort by price
        List<Trip> allTrips = new ArrayList<>();
        allTrips.addAll(directTrips);
        allTrips.addAll(connectingTrips);
        
        allTrips.sort(Comparator.comparing(Trip::getTotalPrice));
        
        return new SearchResponse(allTrips, request);
    }
    
    private List<Trip> findDirectTrips(List<Flight> allFlights, String sourceCode, String destinationCode, LocalDateTime departureTime) {
        return allFlights.stream()
                .filter(flight -> flight.getSourceAirport().getCode().equals(sourceCode) &&
                                flight.getDestinationAirport().getCode().equals(destinationCode) &&
                                (departureTime == null || flight.getDepartureTime().isAfter(departureTime.minusHours(1))))
                .flatMap(flight -> flight.getFlightAirlines().stream()
                        .map(flightAirline -> createTrip(flight, flightAirline.getAirline())))
                .collect(Collectors.toList());
    }
    
    private List<Trip> findConnectingTrips(List<Flight> allFlights, String sourceCode, String destinationCode, LocalDateTime departureTime) {
        List<Trip> connectingTrips = new ArrayList<>();
        
        // Find first leg flights
        List<Flight> firstLegFlights = allFlights.stream()
                .filter(flight -> flight.getSourceAirport().getCode().equals(sourceCode) &&
                                (departureTime == null || flight.getDepartureTime().isAfter(departureTime.minusHours(1))))
                .collect(Collectors.toList());
        
        for (Flight firstLeg : firstLegFlights) {
            // Find second leg flights
            List<Flight> secondLegFlights = allFlights.stream()
                    .filter(flight -> flight.getSourceAirport().getCode().equals(firstLeg.getDestinationAirport().getCode()) &&
                                    flight.getDestinationAirport().getCode().equals(destinationCode) &&
                                    flight.getDepartureTime().isAfter(firstLeg.getArrivalTime()))
                    .collect(Collectors.toList());
            
            for (Flight secondLeg : secondLegFlights) {
                // Find common airlines
                Set<Airline> commonAirlines = firstLeg.getFlightAirlines().stream()
                        .map(flightAirline -> flightAirline.getAirline())
                        .filter(airline -> secondLeg.getFlightAirlines().stream()
                                .anyMatch(flightAirline -> flightAirline.getAirline().equals(airline)))
                        .collect(Collectors.toSet());
                
                for (Airline airline : commonAirlines) {
                    Trip trip = createConnectingTrip(firstLeg, secondLeg, airline);
                    if (trip != null) {
                        connectingTrips.add(trip);
                    }
                }
            }
        }
        
        return connectingTrips;
    }
    
    private Trip createTrip(Flight flight, Airline airline) {
        BigDecimal price = calculateFarePrice(flight, airline, 1);
        if (price == null) {
            return null;
        }
        
        Trip trip = new Trip();
        trip.setAirline(airline.getCode());
        trip.setTotalPrice(price);
        trip.setFlights(List.of(flight));
        trip.setTotalDuration(flight.getDurationInMinutes());
        
        return trip;
    }
    
    private Trip createConnectingTrip(Flight firstLeg, Flight secondLeg, Airline airline) {
        BigDecimal price = calculateFarePrice(firstLeg, airline, 2);
        if (price == null) {
            return null;
        }
        
        Trip trip = new Trip();
        trip.setAirline(airline.getCode());
        trip.setTotalPrice(price);
        trip.setFlights(List.of(firstLeg, secondLeg));
        trip.setTotalDuration(firstLeg.getDurationInMinutes() + secondLeg.getDurationInMinutes());
        
        return trip;
    }
    
    private BigDecimal calculateFarePrice(Flight flight, Airline airline, int legCount) {
        List<Fare> fares = fareRepository.findActiveFaresByAirlineCode(airline.getCode());
        
        BigDecimal bestPrice = null;
        
        for (Fare fare : fares) {
            if (isFareApplicable(fare, flight, legCount)) {
                if (bestPrice == null || fare.getBasePrice().compareTo(bestPrice) < 0) {
                    bestPrice = fare.getBasePrice();
                }
            }
        }
        
        return bestPrice;
    }
    
    private boolean isFareApplicable(Fare fare, Flight flight, int legCount) {
        // Check if fare has no restrictions (base fare)
        if (fare.getRestrictions().isEmpty()) {
            return true;
        }
        
        // Check each restriction
        for (FareRestriction restriction : fare.getRestrictions()) {
            if (!isRestrictionSatisfied(restriction, flight, legCount)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isRestrictionSatisfied(FareRestriction restriction, Flight flight, int legCount) {
        switch (restriction.getRestrictionType()) {
            case ENDPOINT:
                return flight.getSourceAirport().getCode().equals(restriction.getRestrictionValue()) ||
                       flight.getDestinationAirport().getCode().equals(restriction.getRestrictionValue());
            
            case DEPARTURE_TIME:
                LocalTime departureTime = flight.getDepartureTime().toLocalTime();
                LocalTime restrictionTime = LocalTime.parse(restriction.getRestrictionValue());
                return departureTime.isBefore(restrictionTime);
            
            case MULTI_LEG:
                int requiredLegs = Integer.parseInt(restriction.getRestrictionValue());
                return legCount >= requiredLegs;
            
            default:
                return false;
        }
    }
}

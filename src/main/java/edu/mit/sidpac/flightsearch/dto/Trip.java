package edu.mit.sidpac.flightsearch.dto;

import edu.mit.sidpac.flightsearch.entity.Flight;

import java.math.BigDecimal;
import java.util.List;

public class Trip {
    
    private String airline;
    private BigDecimal totalPrice;
    private List<Flight> flights;
    private long totalDuration; // in minutes
    
    // Constructors
    public Trip() {}
    
    public Trip(String airline, BigDecimal totalPrice, List<Flight> flights, long totalDuration) {
        this.airline = airline;
        this.totalPrice = totalPrice;
        this.flights = flights;
        this.totalDuration = totalDuration;
    }
    
    // Getters and Setters
    public String getAirline() {
        return airline;
    }
    
    public void setAirline(String airline) {
        this.airline = airline;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public List<Flight> getFlights() {
        return flights;
    }
    
    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }
    
    public long getTotalDuration() {
        return totalDuration;
    }
    
    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }
    
    // Utility methods
    public String getRoute() {
        if (flights == null || flights.isEmpty()) {
            return "";
        }
        
        if (flights.size() == 1) {
            return flights.get(0).getRoute();
        }
        
        return flights.get(0).getSourceAirport().getCode() + " → " + 
               flights.get(flights.size() - 1).getDestinationAirport().getCode();
    }
    
    public boolean isDirect() {
        return flights != null && flights.size() == 1;
    }
    
    public int getLegCount() {
        return flights != null ? flights.size() : 0;
    }
}

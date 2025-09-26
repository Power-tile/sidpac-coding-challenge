package edu.mit.sidpac.flightsearch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class SearchRequest {
    
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code must be 3 uppercase letters")
    private String sourceAirport;
    
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code must be 3 uppercase letters")
    private String destinationAirport;
    
    private LocalDateTime departureTime;
    
    // Constructors
    public SearchRequest() {}
    
    public SearchRequest(String sourceAirport, String destinationAirport, LocalDateTime departureTime) {
        this.sourceAirport = sourceAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
    }
    
    // Getters and Setters
    public String getSourceAirport() {
        return sourceAirport;
    }
    
    public void setSourceAirport(String sourceAirport) {
        this.sourceAirport = sourceAirport;
    }
    
    public String getDestinationAirport() {
        return destinationAirport;
    }
    
    public void setDestinationAirport(String destinationAirport) {
        this.destinationAirport = destinationAirport;
    }
    
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
}

package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "flights")
public class Flight extends BaseEntity {
    
    @NotBlank
    @Size(max = 10)
    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_airport_id", nullable = false)
    private Airport sourceAirport;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destinationAirport;
    
    @NotNull
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;
    
    @NotNull
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;
    
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<FlightAirline> flightAirlines = new HashSet<>();
    
    // Constructors
    public Flight() {}
    
    public Flight(String flightNumber, Airport sourceAirport, Airport destinationAirport, 
                  LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightNumber = flightNumber;
        this.sourceAirport = sourceAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }
    
    // Getters and Setters
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public Airport getSourceAirport() {
        return sourceAirport;
    }
    
    public void setSourceAirport(Airport sourceAirport) {
        this.sourceAirport = sourceAirport;
    }
    
    public Airport getDestinationAirport() {
        return destinationAirport;
    }
    
    public void setDestinationAirport(Airport destinationAirport) {
        this.destinationAirport = destinationAirport;
    }
    
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public Set<FlightAirline> getFlightAirlines() {
        return flightAirlines;
    }
    
    public void setFlightAirlines(Set<FlightAirline> flightAirlines) {
        this.flightAirlines = flightAirlines;
    }
    
    // Utility methods
    public long getDurationInMinutes() {
        return java.time.Duration.between(departureTime, arrivalTime).toMinutes();
    }
    
    public String getRoute() {
        return sourceAirport.getCode() + " → " + destinationAirport.getCode();
    }
    
    // JSON serialization helpers
    public String getSourceAirportCode() {
        return sourceAirport != null ? sourceAirport.getCode() : null;
    }
    
    public String getDestinationAirportCode() {
        return destinationAirport != null ? destinationAirport.getCode() : null;
    }
    
    public String getAirlineCode() {
        if (flightAirlines != null && !flightAirlines.isEmpty()) {
            return flightAirlines.iterator().next().getAirline().getCode();
        }
        return null;
    }
}

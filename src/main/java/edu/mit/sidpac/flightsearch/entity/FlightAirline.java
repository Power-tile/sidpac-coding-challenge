package edu.mit.sidpac.flightsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "flight_airlines")
public class FlightAirline extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;
    
    // Constructors
    public FlightAirline() {}
    
    public FlightAirline(Flight flight, Airline airline) {
        this.flight = flight;
        this.airline = airline;
    }
    
    // Getters and Setters
    @JsonIgnore
    public Flight getFlight() {
        return flight;
    }
    
    public void setFlight(Flight flight) {
        this.flight = flight;
    }
    
    public Airline getAirline() {
        return airline;
    }
    
    public void setAirline(Airline airline) {
        this.airline = airline;
    }
    
    // Override equals and hashCode for proper uniqueness
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightAirline that = (FlightAirline) o;
        return flight != null && airline != null && 
               flight.equals(that.flight) && airline.equals(that.airline);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(flight, airline);
    }
}

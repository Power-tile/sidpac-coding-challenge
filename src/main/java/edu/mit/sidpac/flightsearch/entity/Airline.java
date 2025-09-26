package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "airlines")
public class Airline extends BaseEntity {
    
    @NotBlank
    @Size(max = 3)
    @Column(unique = true, nullable = false, length = 3)
    private String code;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 50)
    @Column(length = 50)
    private String country;
    
    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FlightAirline> flightAirlines = new HashSet<>();
    
    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Fare> fares = new HashSet<>();
    
    // Constructors
    public Airline() {}
    
    public Airline(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
    }
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public Set<FlightAirline> getFlightAirlines() {
        return flightAirlines;
    }
    
    public void setFlightAirlines(Set<FlightAirline> flightAirlines) {
        this.flightAirlines = flightAirlines;
    }
    
    public Set<Fare> getFares() {
        return fares;
    }
    
    public void setFares(Set<Fare> fares) {
        this.fares = fares;
    }
}

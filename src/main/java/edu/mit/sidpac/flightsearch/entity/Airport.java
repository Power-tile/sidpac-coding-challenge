package edu.mit.sidpac.flightsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "airports")
public class Airport extends BaseEntity {
    
    @NotBlank
    @Size(max = 3)
    @Column(unique = true, nullable = false, length = 3)
    private String code;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String city;
    
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String country;
    
    @OneToMany(mappedBy = "sourceAirport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Flight> departingFlights = new HashSet<>();
    
    @OneToMany(mappedBy = "destinationAirport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Flight> arrivingFlights = new HashSet<>();
    
    // Constructors
    public Airport() {}
    
    public Airport(String code, String name, String city, String country) {
        this.code = code;
        this.name = name;
        this.city = city;
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
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @JsonIgnore
    public Set<Flight> getDepartingFlights() {
        return departingFlights;
    }
    
    public void setDepartingFlights(Set<Flight> departingFlights) {
        this.departingFlights = departingFlights;
    }
    
    @JsonIgnore
    public Set<Flight> getArrivingFlights() {
        return arrivingFlights;
    }
    
    public void setArrivingFlights(Set<Flight> arrivingFlights) {
        this.arrivingFlights = arrivingFlights;
    }
    
    // Utility methods
    public String getLocation() {
        return city + ", " + country;
    }
}

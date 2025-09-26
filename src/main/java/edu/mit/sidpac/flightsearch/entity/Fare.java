package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fares")
public class Fare extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    private Airline airline;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "fare_name", nullable = false, length = 100)
    private String fareName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "fare", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FareRestriction> restrictions = new HashSet<>();
    
    // Constructors
    public Fare() {}
    
    public Fare(Airline airline, BigDecimal basePrice, String fareName, String description) {
        this.airline = airline;
        this.basePrice = basePrice;
        this.fareName = fareName;
        this.description = description;
    }
    
    // Getters and Setters
    public Airline getAirline() {
        return airline;
    }
    
    public void setAirline(Airline airline) {
        this.airline = airline;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public String getFareName() {
        return fareName;
    }
    
    public void setFareName(String fareName) {
        this.fareName = fareName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Set<FareRestriction> getRestrictions() {
        return restrictions;
    }
    
    public void setRestrictions(Set<FareRestriction> restrictions) {
        this.restrictions = restrictions;
    }
}

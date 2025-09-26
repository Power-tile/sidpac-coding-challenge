package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "fare_restrictions")
public class FareRestriction extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fare_id", nullable = false)
    private Fare fare;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false)
    private RestrictionType restrictionType;
    
    @NotBlank
    @Column(name = "restriction_value", nullable = false)
    private String restrictionValue;
    
    // Constructors
    public FareRestriction() {}
    
    public FareRestriction(Fare fare, RestrictionType restrictionType, String restrictionValue) {
        this.fare = fare;
        this.restrictionType = restrictionType;
        this.restrictionValue = restrictionValue;
    }
    
    // Getters and Setters
    public Fare getFare() {
        return fare;
    }
    
    public void setFare(Fare fare) {
        this.fare = fare;
    }
    
    public RestrictionType getRestrictionType() {
        return restrictionType;
    }
    
    public void setRestrictionType(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }
    
    public String getRestrictionValue() {
        return restrictionValue;
    }
    
    public void setRestrictionValue(String restrictionValue) {
        this.restrictionValue = restrictionValue;
    }
}

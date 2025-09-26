package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.Airline;
import edu.mit.sidpac.flightsearch.entity.Fare;
import edu.mit.sidpac.flightsearch.entity.FareRestriction;
import edu.mit.sidpac.flightsearch.entity.RestrictionType;
import edu.mit.sidpac.flightsearch.repository.AirlineRepository;
import edu.mit.sidpac.flightsearch.repository.FareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class FareService {
    
    @Autowired
    private FareRepository fareRepository;
    
    @Autowired
    private AirlineRepository airlineRepository;
    
    public List<Fare> getAllFares() {
        return fareRepository.findByIsActiveTrue();
    }
    
    public List<Fare> getFaresByAirline(String airlineCode) {
        return fareRepository.findActiveFaresByAirlineCode(airlineCode);
    }
    
    public Optional<Fare> getFareById(String id) {
        return fareRepository.findById(id).filter(Fare::getIsActive);
    }
    
    public Fare createFare(String airlineCode, BigDecimal basePrice, String fareName, 
                          String description, Set<FareRestrictionData> restrictions) {
        
        Airline airline = airlineRepository.findByCodeAndIsActiveTrue(airlineCode)
                .orElseThrow(() -> new RuntimeException("Airline not found: " + airlineCode));
        
        Fare fare = new Fare(airline, basePrice, fareName, description);
        fare = fareRepository.save(fare);
        
        // Add restrictions
        for (FareRestrictionData restrictionData : restrictions) {
            FareRestriction restriction = new FareRestriction(
                    fare, 
                    restrictionData.getType(), 
                    restrictionData.getValue()
            );
            fare.getRestrictions().add(restriction);
        }
        
        return fareRepository.save(fare);
    }
    
    public Fare updateFare(String id, BigDecimal basePrice, String fareName, 
                          String description, Set<FareRestrictionData> restrictions) {
        
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fare not found: " + id));
        
        if (!fare.getIsActive()) {
            throw new RuntimeException("Fare not found: " + id);
        }
        
        fare.setBasePrice(basePrice);
        fare.setFareName(fareName);
        fare.setDescription(description);
        
        // Update restrictions
        fare.getRestrictions().clear();
        for (FareRestrictionData restrictionData : restrictions) {
            FareRestriction restriction = new FareRestriction(
                    fare, 
                    restrictionData.getType(), 
                    restrictionData.getValue()
            );
            fare.getRestrictions().add(restriction);
        }
        
        return fareRepository.save(fare);
    }
    
    public void deleteFare(String id) {
        Fare fare = fareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fare not found: " + id));
        
        fare.setIsActive(false);
        fareRepository.save(fare);
    }
    
    public static class FareRestrictionData {
        private RestrictionType type;
        private String value;
        
        public FareRestrictionData() {}
        
        public FareRestrictionData(RestrictionType type, String value) {
            this.type = type;
            this.value = value;
        }
        
        public RestrictionType getType() {
            return type;
        }
        
        public void setType(RestrictionType type) {
            this.type = type;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }
}

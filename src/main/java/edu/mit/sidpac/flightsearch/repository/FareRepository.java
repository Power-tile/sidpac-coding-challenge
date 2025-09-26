package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.Fare;
import edu.mit.sidpac.flightsearch.entity.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FareRepository extends JpaRepository<Fare, String> {
    
    @Query("SELECT f FROM Fare f WHERE f.airline.code = :airlineCode")
    List<Fare> findFaresByAirlineCode(@Param("airlineCode") String airlineCode);
    
    @Query("SELECT f FROM Fare f " +
           "JOIN f.restrictions r " +
           "WHERE f.airline.code = :airlineCode " +
           "AND r.restrictionType = :restrictionType")
    List<Fare> findFaresByAirlineAndRestrictionType(@Param("airlineCode") String airlineCode, 
                                                   @Param("restrictionType") RestrictionType restrictionType);
    
    @Query("SELECT f FROM Fare f " +
           "JOIN f.restrictions r " +
           "WHERE f.airline.code = :airlineCode " +
           "AND r.restrictionType = :restrictionType AND r.restrictionValue = :restrictionValue")
    List<Fare> findFaresByAirlineAndRestriction(@Param("airlineCode") String airlineCode, 
                                               @Param("restrictionType") RestrictionType restrictionType,
                                               @Param("restrictionValue") String restrictionValue);
    
    @Query("SELECT f FROM Fare f WHERE f.airline.code = :airlineCode " +
           "AND f.basePrice BETWEEN :minPrice AND :maxPrice")
    List<Fare> findFaresByAirlineAndPriceRange(@Param("airlineCode") String airlineCode, 
                                              @Param("minPrice") BigDecimal minPrice, 
                                              @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT f FROM Fare f WHERE f.airline.code = :airlineCode " +
           "AND NOT EXISTS (SELECT r FROM FareRestriction r WHERE r.fare = f)")
    List<Fare> findFaresWithoutRestrictions(@Param("airlineCode") String airlineCode);
}

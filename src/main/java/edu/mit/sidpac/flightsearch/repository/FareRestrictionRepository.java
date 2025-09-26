package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.FareRestriction;
import edu.mit.sidpac.flightsearch.entity.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FareRestrictionRepository extends JpaRepository<FareRestriction, String> {
    
    List<FareRestriction> findByFareId(String fareId);
    
    List<FareRestriction> findByRestrictionType(RestrictionType restrictionType);
    
    @Query("SELECT r FROM FareRestriction r WHERE r.fare.airline.code = :airlineCode AND r.restrictionType = :restrictionType")
    List<FareRestriction> findByAirlineAndRestrictionType(@Param("airlineCode") String airlineCode, 
                                                         @Param("restrictionType") RestrictionType restrictionType);
    
    @Query("SELECT r FROM FareRestriction r WHERE r.fare.airline.code = :airlineCode " +
           "AND r.restrictionType = :restrictionType AND r.restrictionValue = :restrictionValue")
    List<FareRestriction> findByAirlineAndRestriction(@Param("airlineCode") String airlineCode, 
                                                     @Param("restrictionType") RestrictionType restrictionType,
                                                     @Param("restrictionValue") String restrictionValue);
}

package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    
    Optional<Airport> findByCode(String code);
    
    Optional<Airport> findByCodeAndIsActiveTrue(String code);
    
    List<Airport> findByIsActiveTrue();
    
    List<Airport> findByCityAndIsActiveTrue(String city);
    
    List<Airport> findByCountryAndIsActiveTrue(String country);
    
    @Query("SELECT a FROM Airport a WHERE a.isActive = true AND (a.code LIKE %:searchTerm% OR a.name LIKE %:searchTerm% OR a.city LIKE %:searchTerm%)")
    List<Airport> findBySearchTermAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    boolean existsByCode(String code);
}

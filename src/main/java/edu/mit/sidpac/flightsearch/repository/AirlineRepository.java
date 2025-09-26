package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, String> {
    
    Optional<Airline> findByCode(String code);
    
    Optional<Airline> findByCodeAndIsActiveTrue(String code);
    
    List<Airline> findByIsActiveTrue();
    
    List<Airline> findByCountryAndIsActiveTrue(String country);
    
    @Query("SELECT a FROM Airline a WHERE a.isActive = true AND (a.code LIKE %:searchTerm% OR a.name LIKE %:searchTerm%)")
    List<Airline> findBySearchTermAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    boolean existsByCode(String code);
}

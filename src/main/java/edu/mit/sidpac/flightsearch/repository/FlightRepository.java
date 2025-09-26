package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {
    
    List<Flight> findByIsActiveTrue();
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.sourceAirport.code = :sourceCode AND f.destinationAirport.code = :destinationCode")
    List<Flight> findDirectFlights(@Param("sourceCode") String sourceCode, @Param("destinationCode") String destinationCode);
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.sourceAirport.code = :sourceCode")
    List<Flight> findFlightsFromAirport(@Param("sourceCode") String sourceCode);
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.destinationAirport.code = :destinationCode")
    List<Flight> findFlightsToAirport(@Param("destinationCode") String destinationCode);
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.departureTime >= :startTime AND f.departureTime <= :endTime")
    List<Flight> findFlightsByDepartureTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.sourceAirport.code = :sourceCode AND f.departureTime >= :departureTime")
    List<Flight> findFlightsFromAirportAfterTime(@Param("sourceCode") String sourceCode, @Param("departureTime") LocalDateTime departureTime);
    
    @Query("SELECT f FROM Flight f WHERE f.isActive = true AND f.destinationAirport.code = :destinationCode AND f.arrivalTime <= :arrivalTime")
    List<Flight> findFlightsToAirportBeforeTime(@Param("destinationCode") String destinationCode, @Param("arrivalTime") LocalDateTime arrivalTime);
    
    @Query("SELECT DISTINCT f FROM Flight f " +
           "JOIN f.flightAirlines fa " +
           "WHERE f.isActive = true AND fa.airline.code = :airlineCode")
    List<Flight> findFlightsByAirline(@Param("airlineCode") String airlineCode);
    
    @Query("SELECT f FROM Flight f " +
           "JOIN f.flightAirlines fa " +
           "WHERE f.isActive = true AND fa.airline.code = :airlineCode " +
           "AND f.sourceAirport.code = :sourceCode AND f.destinationAirport.code = :destinationCode")
    List<Flight> findFlightsByAirlineAndRoute(@Param("airlineCode") String airlineCode, 
                                            @Param("sourceCode") String sourceCode, 
                                            @Param("destinationCode") String destinationCode);
}

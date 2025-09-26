package edu.mit.sidpac.flightsearch.controller;

import edu.mit.sidpac.flightsearch.dto.SearchRequest;
import edu.mit.sidpac.flightsearch.dto.SearchResponse;
import edu.mit.sidpac.flightsearch.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Flight Search", description = "Flight search and pricing endpoints")
public class SearchController {
    
    @Autowired
    private FlightSearchService flightSearchService;
    
    @PostMapping("/flights")
    @Operation(summary = "Search flights with pricing", description = "Search for flights between airports with calculated pricing")
    public ResponseEntity<SearchResponse> searchFlights(@Valid @RequestBody SearchRequest request) {
        try {
            SearchResponse response = flightSearchService.searchFlights(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/flights")
    @Operation(summary = "Search flights with query parameters", description = "Search for flights using query parameters")
    public ResponseEntity<SearchResponse> searchFlightsWithParams(
            @RequestParam String sourceAirport,
            @RequestParam String destinationAirport,
            @RequestParam(required = false) String departureTime) {
        
        try {
            SearchRequest request = new SearchRequest();
            request.setSourceAirport(sourceAirport);
            request.setDestinationAirport(destinationAirport);
            
            if (departureTime != null) {
                request.setDepartureTime(java.time.LocalDateTime.parse(departureTime));
            }
            
            SearchResponse response = flightSearchService.searchFlights(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

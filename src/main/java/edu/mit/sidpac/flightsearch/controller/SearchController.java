package edu.mit.sidpac.flightsearch.controller;

import edu.mit.sidpac.flightsearch.dto.SearchRequest;
import edu.mit.sidpac.flightsearch.dto.SearchResponse;
import edu.mit.sidpac.flightsearch.service.FlightSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
public class SearchController {
    
    @Autowired
    private FlightSearchService flightSearchService;
    
    @GetMapping("/planning")
    public ResponseEntity<SearchResponse> searchFlights(
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

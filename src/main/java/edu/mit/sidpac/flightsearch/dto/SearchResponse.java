package edu.mit.sidpac.flightsearch.dto;

import java.util.List;

public class SearchResponse {
    
    private List<Trip> trips;
    private SearchRequest searchCriteria;
    private int totalResults;
    
    // Constructors
    public SearchResponse() {}
    
    public SearchResponse(List<Trip> trips, SearchRequest searchCriteria) {
        this.trips = trips;
        this.searchCriteria = searchCriteria;
        this.totalResults = trips.size();
    }
    
    // Getters and Setters
    public List<Trip> getTrips() {
        return trips;
    }
    
    public void setTrips(List<Trip> trips) {
        this.trips = trips;
        this.totalResults = trips.size();
    }
    
    public SearchRequest getSearchCriteria() {
        return searchCriteria;
    }
    
    public void setSearchCriteria(SearchRequest searchCriteria) {
        this.searchCriteria = searchCriteria;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}

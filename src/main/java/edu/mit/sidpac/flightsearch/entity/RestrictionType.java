package edu.mit.sidpac.flightsearch.entity;

public enum RestrictionType {
    ENDPOINT,        // Airport code restrictions (source/destination)
    DEPARTURE_TIME,  // Time-based restrictions
    MULTI_LEG        // Multi-leg trip restrictions
}

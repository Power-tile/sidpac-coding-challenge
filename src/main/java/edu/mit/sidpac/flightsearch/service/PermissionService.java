package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    
    public boolean canManageFlights(User user, String airlineCode) {
        if (user == null) return false;
        
        // Only ADMIN exists. Super admin (no assigned airline) can manage all.
        // Airline admin can manage only their assigned airline.
        return user.getRole() == UserRole.ADMIN &&
               (user.getAssignedAirlineCode() == null || user.getAssignedAirlineCode().equals(airlineCode));
    }
    
    public boolean canManageFares(User user, String airlineCode) {
        return canManageFlights(user, airlineCode);
    }
    
    public boolean canViewAllData(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }
    
    public boolean canSearchFlights(User user) {
        // Search is public now; this method not used for gating
        return true;
    }
    
    public boolean isSuperAdmin(User user) {
        if (user == null) return false;
        
        return user.getRole() == UserRole.ADMIN && user.getAssignedAirlineCode() == null;
    }
    
    public boolean isAirlineAdmin(User user) {
        if (user == null) return false;
        
        return user.getRole() == UserRole.ADMIN && user.getAssignedAirlineCode() != null;
    }
}

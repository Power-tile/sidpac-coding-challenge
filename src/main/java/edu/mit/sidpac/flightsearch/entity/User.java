package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.ADMIN;
    
    @Column(name = "assigned_airline_code", length = 3)
    private String assignedAirlineCode;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserSession> sessions = new HashSet<>();
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String passwordHash, String firstName, String lastName, UserRole role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    public User(String username, String email, String passwordHash, String firstName, String lastName, UserRole role, String assignedAirlineCode) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.assignedAirlineCode = assignedAirlineCode;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Set<UserSession> getSessions() {
        return sessions;
    }
    
    public void setSessions(Set<UserSession> sessions) {
        this.sessions = sessions;
    }
    
    public String getAssignedAirlineCode() {
        return assignedAirlineCode;
    }
    
    public void setAssignedAirlineCode(String assignedAirlineCode) {
        this.assignedAirlineCode = assignedAirlineCode;
    }
    
    // Utility methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }
    
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
    
    public boolean isAirlineAdmin() {
        return hasRole(UserRole.ADMIN) && assignedAirlineCode != null;
    }
    
    public boolean canManageAirline(String airlineCode) {
        if (hasRole(UserRole.ADMIN)) {
            return assignedAirlineCode == null || assignedAirlineCode.equals(airlineCode);
        }
        return false;
    }
}

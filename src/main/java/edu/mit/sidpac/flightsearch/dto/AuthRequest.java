package edu.mit.sidpac.flightsearch.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    
    @NotBlank
    private String usernameOrEmail;
    
    @NotBlank
    private String password;
    
    // Constructors
    public AuthRequest() {}
    
    public AuthRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

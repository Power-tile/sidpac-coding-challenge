package edu.mit.sidpac.flightsearch.controller;

import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.AuthResponse;
import edu.mit.sidpac.flightsearch.dto.RegisterRequest;
import edu.mit.sidpac.flightsearch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, 
                                                @RequestHeader(value = "X-Session-ID", required = false) String sessionId) {
        try {
            AuthResponse response = authService.register(request, sessionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("session") || e.getMessage().contains("authentication")) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            authService.logout(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

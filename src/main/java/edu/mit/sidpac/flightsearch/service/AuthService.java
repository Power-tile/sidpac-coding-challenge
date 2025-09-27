package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.AuthResponse;
import edu.mit.sidpac.flightsearch.dto.RegisterRequest;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import edu.mit.sidpac.flightsearch.entity.UserSession;
import edu.mit.sidpac.flightsearch.repository.UserRepository;
import edu.mit.sidpac.flightsearch.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    public AuthResponse login(AuthRequest request) {
        // Authenticate user using Spring Security
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(), 
                request.getPassword()
            )
        );
        
        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Get user from repository
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete existing sessions
        userSessionRepository.deleteAllSessionsByUserId(user.getId());
        
        // Create session with session ID
        String sessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession(
                user,
                sessionId,
                "no-refresh-token", // Default value for session-based auth
                LocalDateTime.now().plusHours(24), // 24 hour session
                LocalDateTime.now().plusHours(24) // Same expiration as session
        );
        userSessionRepository.save(session);
        
        return new AuthResponse(sessionId, null, user.getRole().name());
    }
    
    public AuthResponse register(RegisterRequest request, String sessionId) {
        // Check if session ID is provided
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new RuntimeException("Authentication required - session ID missing");
        }
        
        // Check if the current user is a super admin (no assigned airline code)
        User currentUser = getCurrentUser(sessionId);
        if (currentUser.getAssignedAirlineCode() != null) {
            throw new RuntimeException("Only super admins can register new users");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                UserRole.ADMIN
        );
        
        userRepository.save(user);
        
        // Create session for new user
        String newSessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession(
                user,
                newSessionId,
                "no-refresh-token", // Default value for session-based auth
                LocalDateTime.now().plusHours(24), // 24 hour session
                LocalDateTime.now().plusHours(24) // Same expiration as session
        );
        userSessionRepository.save(session);
        
        return new AuthResponse(newSessionId, null, user.getRole().name());
    }
    
    @Transactional
    public void logout(String sessionId) {
        // Check if session exists before attempting to delete
        Optional<UserSession> sessionOpt = userSessionRepository.findByTokenHash(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Invalid session ID");
        }
        
        // Delete the session directly using the token hash
        // This is more efficient and ensures immediate deletion
        userSessionRepository.deleteByTokenHash(sessionId);
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getCurrentUser(String sessionId) {
        UserSession session = userSessionRepository.findByTokenHash(sessionId)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        
        if (session.isExpired()) {
            userSessionRepository.delete(session);
            throw new RuntimeException("Session expired");
        }
        
        return session.getUser();
    }
}

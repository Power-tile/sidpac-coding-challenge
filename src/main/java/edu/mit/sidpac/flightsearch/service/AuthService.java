package edu.mit.sidpac.flightsearch.service;

import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.AuthResponse;
import edu.mit.sidpac.flightsearch.dto.RegisterRequest;
import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import edu.mit.sidpac.flightsearch.entity.UserSession;
import edu.mit.sidpac.flightsearch.repository.UserRepository;
import edu.mit.sidpac.flightsearch.repository.UserSessionRepository;
import edu.mit.sidpac.flightsearch.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private JwtUtil jwtUtil;
    
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsernameOrEmailAndIsActiveTrue(request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // Deactivate existing sessions
        userSessionRepository.deactivateAllSessionsByUserId(user.getId());
        
        // Generate tokens
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        // Create session
        UserSession session = new UserSession(
                user,
                jwtUtil.hashToken(token),
                jwtUtil.hashToken(refreshToken),
                LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000),
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000)
        );
        userSessionRepository.save(session);
        
        return new AuthResponse(token, refreshToken, user.getRole().name());
    }
    
    public AuthResponse register(RegisterRequest request) {
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
        
        // Generate tokens for new user
        String token = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        // Create session
        UserSession session = new UserSession(
                user,
                jwtUtil.hashToken(token),
                jwtUtil.hashToken(refreshToken),
                LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000),
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000)
        );
        userSessionRepository.save(session);
        
        return new AuthResponse(token, refreshToken, user.getRole().name());
    }
    
    public void logout(String token) {
        String tokenHash = jwtUtil.hashToken(token);
        userSessionRepository.findByTokenHashAndIsActiveTrue(tokenHash)
                .ifPresent(session -> {
                    session.setIsActive(false);
                    userSessionRepository.save(session);
                });
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        String refreshTokenHash = jwtUtil.hashToken(refreshToken);
        UserSession session = userSessionRepository.findByRefreshTokenHashAndIsActiveTrue(refreshTokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (session.isRefreshExpired()) {
            session.setIsActive(false);
            userSessionRepository.save(session);
            throw new RuntimeException("Refresh token expired");
        }
        
        User user = session.getUser();
        
        // Generate new tokens
        String newToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        
        // Update session
        session.setTokenHash(jwtUtil.hashToken(newToken));
        session.setRefreshTokenHash(jwtUtil.hashToken(newRefreshToken));
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime() / 1000));
        session.setRefreshExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000));
        userSessionRepository.save(session);
        
        return new AuthResponse(newToken, newRefreshToken, user.getRole().name());
    }
    
    public User getCurrentUser(String token) {
        String tokenHash = jwtUtil.hashToken(token);
        UserSession session = userSessionRepository.findByTokenHashAndIsActiveTrue(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        if (session.isExpired()) {
            session.setIsActive(false);
            userSessionRepository.save(session);
            throw new RuntimeException("Token expired");
        }
        
        return session.getUser();
    }
}

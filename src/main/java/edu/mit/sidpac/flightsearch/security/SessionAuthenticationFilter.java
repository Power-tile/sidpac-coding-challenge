package edu.mit.sidpac.flightsearch.security;

import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserSession;
import edu.mit.sidpac.flightsearch.repository.UserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String sessionId = request.getHeader("X-Session-ID");
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        System.out.println("SessionAuthenticationFilter: " + method + " " + path + " - SessionID: " + sessionId);
        
        // Check if this is a protected endpoint that requires authentication
        boolean requiresAuth = isProtectedEndpoint(request);
        
        System.out.println("SessionAuthenticationFilter: requiresAuth = " + requiresAuth);
        
        if (requiresAuth) {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                // Missing session header for protected endpoint
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            try {
                UserSession session = userSessionRepository.findByTokenHash(sessionId)
                        .orElse(null);
                
                if (session == null || session.isExpired()) {
                    // Invalid or expired session
                    if (session != null && session.isExpired()) {
                        // Clean up expired session
                        userSessionRepository.delete(session);
                    }
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                User user = session.getUser();
                
                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        user.getUsername(), 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Session is invalid
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else if (sessionId != null && !sessionId.trim().isEmpty()) {
            // For public endpoints, still try to authenticate if session is provided
            try {
                UserSession session = userSessionRepository.findByTokenHash(sessionId)
                        .orElse(null);
                
                if (session != null && !session.isExpired()) {
                    User user = session.getUser();
                    
                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            user.getUsername(), 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (session != null && session.isExpired()) {
                    // Clean up expired session
                    userSessionRepository.delete(session);
                }
            } catch (Exception e) {
                // Session is invalid, continue without authentication for public endpoints
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isProtectedEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        // Auth endpoints are always public
        if (path.startsWith("/api/auth/")) {
            return false;
        }
        
        // Search endpoints are always public
        if (path.startsWith("/api/search/")) {
            return false;
        }
        
        // GET operations on flights are public
        if ("GET".equals(method) && path.startsWith("/api/flights")) {
            return false;
        }
        
        // POST, PUT, DELETE operations on flights require authentication
        if (("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) 
            && path.startsWith("/api/flights")) {
            return true;
        }
        
        return false;
    }
}

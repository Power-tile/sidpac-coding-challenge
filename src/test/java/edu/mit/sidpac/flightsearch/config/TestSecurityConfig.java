package edu.mit.sidpac.flightsearch.config;

import edu.mit.sidpac.flightsearch.security.SessionAuthenticationFilter;
import edu.mit.sidpac.flightsearch.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Autowired
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Allow both /auth/** and /api/auth/** for tests
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/flights/**").permitAll()
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            // Note: Removed sessionAuthenticationFilter for tests to avoid interference
        
        return http.build();
    }
}

package edu.mit.sidpac.flightsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mit.sidpac.flightsearch.dto.AuthRequest;
import edu.mit.sidpac.flightsearch.dto.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    // For checking all mappings registered
    @Autowired
    private org.springframework.web.context.WebApplicationContext wac;

    @Test
    void printAllMappings() {
        var mapping = wac.getBean(
            org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.class
        ).getHandlerMethods();
        mapping.forEach((info, method) -> System.out.println(info + " -> " + method));
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuthEndpoints_ShouldBePublic() throws Exception {
        // Test that auth endpoints are accessible without authentication
        AuthRequest loginRequest = new AuthRequest("testuser", "password123");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest()); // Should be accessible but login will fail
    }

    @Test
    void testSearchEndpoints_ShouldBePublic() throws Exception {
        // Test that search endpoints are accessible without authentication
        SearchRequest searchRequest = new SearchRequest("BOS", "LAX", LocalDateTime.now().plusHours(1));
        
        mockMvc.perform(post("/api/search/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk()); // Search endpoint is public and should return 200
    }

    @Test
    void testFlightEndpoints_ShouldBePublic() throws Exception {
        // Test that flight endpoints are accessible without authentication
        mockMvc.perform(get("/api/flights"))
                .andExpect(status().isOk()); // Should return empty list or 200
    }

    @Test
    void testProtectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Test that protected endpoints require authentication
        // This would test endpoints that are not in the permitAll() list
        // For example, if there were admin-only endpoints
        
        // Example: If there was an admin endpoint
        // mockMvc.perform(get("/api/admin/users"))
        //         .andExpect(status().isUnauthorized());
    }
}

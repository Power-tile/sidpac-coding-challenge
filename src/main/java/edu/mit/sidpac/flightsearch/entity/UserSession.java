package edu.mit.sidpac.flightsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
public class UserSession extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(name = "token_hash", nullable = false)
    private String tokenHash;
    
    @NotBlank
    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;
    
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @NotNull
    @Column(name = "refresh_expires_at", nullable = false)
    private LocalDateTime refreshExpiresAt;
    
    // Constructors
    public UserSession() {}
    
    public UserSession(User user, String tokenHash, String refreshTokenHash, 
                      LocalDateTime expiresAt, LocalDateTime refreshExpiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getTokenHash() {
        return tokenHash;
    }
    
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }
    
    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }
    
    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }
    
    public void setRefreshExpiresAt(LocalDateTime refreshExpiresAt) {
        this.refreshExpiresAt = refreshExpiresAt;
    }
    
    // Utility methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isRefreshExpired() {
        return LocalDateTime.now().isAfter(refreshExpiresAt);
    }
}

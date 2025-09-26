package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    
    Optional<UserSession> findByTokenHash(String tokenHash);
    
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    
    List<UserSession> findByUserId(String userId);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = :userId")
    void deleteAllSessionsByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.refreshExpiresAt < :now")
    void deleteExpiredRefreshTokens(@Param("now") LocalDateTime now);
}

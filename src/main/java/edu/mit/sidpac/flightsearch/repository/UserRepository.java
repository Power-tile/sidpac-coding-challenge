package edu.mit.sidpac.flightsearch.repository;

import edu.mit.sidpac.flightsearch.entity.User;
import edu.mit.sidpac.flightsearch.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (u.username = :identifier OR u.email = :identifier)")
    Optional<User> findByUsernameOrEmailAndIsActiveTrue(@Param("identifier") String identifier);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countByRoleAndIsActiveTrue(@Param("role") UserRole role);
}

package javapolis.repository;

import javapolis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    @Query("SELECT u FROM User u WHERE u.verificationTokenExpiry < :now")
    List<User> findExpiredVerificationTokens(@Param("now") LocalDateTime now);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersRegisteredSince(@Param("since") LocalDateTime since);
}

package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes the oldest token for a user using a native query.
     * NOTE: This is database-specific (works on PostgreSQL, MySQL, etc.).
     */
    @Modifying
    @Query(
            value = "DELETE FROM refresh_tokens WHERE ctid IN (SELECT ctid FROM refresh_token WHERE user_id = :userId ORDER BY createdAt ASC LIMIT 1)",
            nativeQuery = true
    )
    void deleteOldestTokenForUser(@Param("userId") UUID userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user.userId = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.token = :token")
    void revokeTokenByValue(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.isRevoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.isRevoked = false AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    boolean existsByToken(String token);
}

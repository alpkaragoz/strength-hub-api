package com.strengthhub.strength_hub_api.security;

import com.strengthhub.strength_hub_api.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId().toString());
        claims.put("isAdmin", user.getIsAdmin());
        claims.put("isCoach", user.isCoach());
        claims.put("isLifter", user.isLifter());
        claims.put("tokenType", "access");

        return createToken(claims, user.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId().toString());
        claims.put("tokenType", "refresh");

        return createToken(claims, user.getUsername(), refreshTokenExpirationMs);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);

            // Validate issuer
            if (!issuer.equals(claims.getIssuer())) {
                log.error("Invalid issuer: {}", claims.getIssuer());
                return false;
            }

            return true;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "access".equals(claims.get("tokenType", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    public boolean isAdminFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class));
    }

    public boolean isCoachFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Boolean.TRUE.equals(claims.get("isCoach", Boolean.class));
    }

    public boolean isLifterFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Boolean.TRUE.equals(claims.get("isLifter", Boolean.class));
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Long getAccessTokenExpirationTime() {
        return accessTokenExpirationMs;
    }

    public Long getRefreshTokenExpirationTime() {
        return refreshTokenExpirationMs;
    }
}
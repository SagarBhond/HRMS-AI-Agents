package com.yourproject.config;

import com.yourproject.entity.Role;
import com.yourproject.entity.User;
import com.yourproject.service.InvalidCredentialsException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Issues and verifies the identity contract shared by Auth Service and the Orchestrator. */
@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    var builder =
        Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .issuedAt(now)
            .expiration(expiry);
    if (user.getEmployeeId() != null) {
      builder.claim("employeeId", user.getEmployeeId());
    }
    return builder.signWith(key).compact();
  }

  public AuthenticatedUser authenticate(String authorizationHeader) {
    if (authorizationHeader == null
        || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
      throw new InvalidCredentialsException("Missing or malformed Authorization header");
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    if (token.isEmpty()) {
      throw new InvalidCredentialsException("Missing bearer token");
    }

    try {
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      Long userId = numberClaim(claims, "userId");
      String username = claims.get("username", String.class);
      String roleValue = claims.get("role", String.class);
      Long employeeId = numberClaim(claims, "employeeId");
      if (userId == null || username == null || roleValue == null) {
        throw new InvalidCredentialsException("Token is missing required identity claims");
      }
      return new AuthenticatedUser(userId, username, Role.valueOf(roleValue), employeeId);
    } catch (InvalidCredentialsException ex) {
      throw ex;
    } catch (JwtException | IllegalArgumentException ex) {
      throw new InvalidCredentialsException("Invalid or expired token");
    }
  }

  private Long numberClaim(Claims claims, String name) {
    Object value = claims.get(name);
    return value instanceof Number number ? number.longValue() : null;
  }

  public record AuthenticatedUser(Long userId, String username, Role role, Long employeeId) {}
}

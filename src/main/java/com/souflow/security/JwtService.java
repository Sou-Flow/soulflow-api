package com.souflow.security;

import com.souflow.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public String extractEmailFromToken(String token) {
    return extractClaim(token, claims -> claims.get("email", String.class));
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
        .signWith(getSigningKey())
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSigningKey() {
    try {
      byte[] keyBytes =
          MessageDigest.getInstance("SHA-256")
              .digest(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Unable to initialize JWT signing key", ex);
    }
  }
}

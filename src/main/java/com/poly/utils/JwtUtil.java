package com.poly.utils;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey; 

@Component
public class JwtUtil {

    private final String SECRET = "blackfloydcantbreathebecausea12kneesonhisneckforusingacounterfeitmoneytobuyabanana"; // ≥32 chars
    private final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    public String generateToken(String username, String roleCode) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roleCode", roleCode)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000)) //2 hours
                //.setExpiration(new Date(System.currentTimeMillis() + 30 * 1000)) //30 seconds
                .signWith(KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return extractAllClaims(token).get("roleCode", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractAllClaims(token); // will throw if invalid/expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
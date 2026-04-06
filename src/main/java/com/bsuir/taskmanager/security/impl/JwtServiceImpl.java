package com.bsuir.taskmanager.security.impl;

import com.bsuir.taskmanager.security.AuthUserDetails;
import com.bsuir.taskmanager.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USER_ID_CLAIM = "userId";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long tokenExpiration;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS);
        if (userDetails instanceof AuthUserDetails authUserDetails) {
            claims.put(USER_ID_CLAIM, authUserDetails.getUserId());
        }

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + tokenExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public void validateToken(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new JwtException("Token expiration is missing");
        }
        if (expiration.before(new Date())) {
            throw new JwtException("Token is expired");
        }
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (tokenType == null || tokenType.isBlank()) {
            throw new JwtException("Token type is missing");
        }
        if (!TOKEN_TYPE_ACCESS.equals(tokenType)) {
            throw new JwtException("Token type is invalid");
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID_CLAIM, Long.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

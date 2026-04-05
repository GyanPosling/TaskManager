package com.bsuir.taskmanager.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    void validateToken(String token);

    String extractUsername(String token);

    Long extractUserId(String token);
}

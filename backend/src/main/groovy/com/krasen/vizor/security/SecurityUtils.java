package com.krasen.vizor.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Component
public class SecurityUtils {
    private final JwtService jwtService;

    public SecurityUtils(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extracts the current authenticated user's ID from the JWT token
     * @return userId from JWT token, or null if not authenticated
     */
    public Long getCurrentUserId() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();
            
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            var jws = jwtService.parse(token);
            Claims claims = jws.getPayload();
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}



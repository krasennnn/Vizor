package com.krasen.vizor.web.DTOs.AuthDTOs;

import java.util.Set;

public record JwtResponse(
        String token,
        Long userId,
        String email,
        String username,
        Set<String> roles
) {}



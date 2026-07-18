package com.hrms.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String email,
        String role
) {
    // Convenience factory
    public static AuthResponse of(String accessToken,
                                  String refreshToken,
                                  String email,
                                  String role,
                                  long expiresIn) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                email,
                role
        );
    }
}
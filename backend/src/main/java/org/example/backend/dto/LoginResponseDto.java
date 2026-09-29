package org.example.backend.dto;

public record LoginResponseDto(
        Long id,
        String ime,
        String prezime,
        String username,
        String email,
        String userType,
        String token
) {
}

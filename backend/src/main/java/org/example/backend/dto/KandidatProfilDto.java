package org.example.backend.dto;

public record KandidatProfilDto(
        Long id,
        String ime,
        String prezime,
        String telefon,
        String username,
        String email
) {
}

package org.example.backend.dto;

public record RegistracijaKandidataDto(
        String ime,
        String prezime,
        String telefon,
        String username,
        String password,
        String email
) {
}

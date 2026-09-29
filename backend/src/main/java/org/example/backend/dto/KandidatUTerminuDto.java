package org.example.backend.dto;

import java.time.LocalDate;

public record KandidatUTerminuDto(
        int redniBroj,
        String ime,
        String prezime,
        String username,
        String email,
        String telefon,
        LocalDate datumKreiranjaPrijave,
        String statusPrijave,
        String statusUplate
) {
}

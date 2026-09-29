package org.example.backend.dto;

import java.time.LocalDate;

public record CenovnikZahtevDto(
        Long tipIspitaId,
        String naziv,
        Double iznos,
        LocalDate vaziOd,
        LocalDate vaziDo
) {
}

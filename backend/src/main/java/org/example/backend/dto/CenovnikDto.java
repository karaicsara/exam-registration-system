package org.example.backend.dto;

import java.time.LocalDate;

public record CenovnikDto(
        Long id,
        Long tipIspitaId,
        String tipIspitaNaziv,
        String naziv,
        Double iznos,
        LocalDate vaziOd,
        LocalDate vaziDo,
        Boolean aktivan
) {
}

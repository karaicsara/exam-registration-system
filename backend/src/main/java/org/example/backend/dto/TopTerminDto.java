package org.example.backend.dto;

import java.time.LocalDate;

public record TopTerminDto(
        Long terminId,
        String tipIspitaNaziv,
        LocalDate datumOdrzavanja,
        String mestoNaziv,
        long brojPrijava
) {
}

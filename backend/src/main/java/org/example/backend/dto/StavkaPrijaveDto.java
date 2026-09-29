package org.example.backend.dto;

import java.time.LocalDate;

public record StavkaPrijaveDto(
        Long id,
        Long ispitniTerminId,
        String tipIspitaNaziv,
        LocalDate datumOdrzavanja,
        String vremePocetka,
        String mestoNaziv,
        Double cenaNaDanPrijave
) {
}

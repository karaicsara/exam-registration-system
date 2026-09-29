package org.example.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record TerminDetaljDto(
        Long terminId,
        String tipIspitaNaziv,
        LocalDate datumOdrzavanja,
        String vremePocetka,
        String mestoNaziv,
        String mestoAdresa,
        int brojPrijavljenih,
        List<KandidatUTerminuDto> kandidati
) {
}

package org.example.backend.dto;

import java.time.LocalDate;

public record IspitniTerminDto(
        Long id,
        Long tipIspitaId,
        String tipIspitaNaziv,
        LocalDate datumOdrzavanja,
        String vremePocetka,
        String napomena,
        Long mestoOdrzavanjaId,
        String mestoNaziv,
        String mestoAdresa,
        Double trenutnaCena,
        Boolean dostupan,
        int brojPrijavljenih,
        boolean mozeSeIzabrati
) {
}

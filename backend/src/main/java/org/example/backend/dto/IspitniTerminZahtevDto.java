package org.example.backend.dto;

import java.time.LocalDate;

public record IspitniTerminZahtevDto(
        LocalDate datumOdrzavanja,
        String vremePocetka,
        String napomena,
        Boolean dostupan,
        Long tipIspitaId,
        Long mestoOdrzavanjaId
) {
}

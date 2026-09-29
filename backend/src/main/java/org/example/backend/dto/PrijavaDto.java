package org.example.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record PrijavaDto(
        Long id,
        LocalDate datumKreiranja,
        Double brutoIznos,
        Double popust,
        Double ukupanIznos,
        String status,
        String napomena,
        boolean mozeSeOtkazati,
        boolean mozeSeDostaviUplata,
        Long kandidatId,
        String kandidatIme,
        String kandidatPrezime,
        String kandidatEmail,
        List<StavkaPrijaveDto> stavke,
        List<UplataDto> uplate
) {
}

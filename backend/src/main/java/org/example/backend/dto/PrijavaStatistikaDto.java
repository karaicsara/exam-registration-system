package org.example.backend.dto;

import java.util.List;

public record PrijavaStatistikaDto(
        List<PrihodOblastiDto> prihodPoOblasti,
        long brojKreiranih,
        long brojPrijavljenih,
        long brojOtkazanih
) {
}

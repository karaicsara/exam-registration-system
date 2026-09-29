package org.example.backend.dto;

import java.util.List;

public record TerminStatistikaDto(
        List<TopTerminDto> najtrazenijiTermini,
        double procenatDostupnihTermina,
        long brojTermina
) {
}

package org.example.backend.dto;

import java.time.LocalDate;

public record UplataDto(
        Long id,
        Long prijavaId,
        String putanjaDokumenta,
        LocalDate datumSlanja,
        String status,
        String napomena
) {
}

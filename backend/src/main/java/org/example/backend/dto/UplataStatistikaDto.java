package org.example.backend.dto;

public record UplataStatistikaDto(
        long brojNaCekanju,
        long brojOdobrenih,
        long brojOdbijenih,
        double procenatOdobrenih
) {
}

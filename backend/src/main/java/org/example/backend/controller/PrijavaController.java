package org.example.backend.controller;

import org.example.backend.dto.PrijavaCreateRequestDto;
import org.example.backend.dto.PrijavaDto;
import org.example.backend.dto.PrijavaStatistikaDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.AdministratorService;
import org.example.backend.service.KandidatService;
import org.example.backend.service.PrijavaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prijave")
@RequiredArgsConstructor
public class PrijavaController {

    private final PrijavaService prijavaService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public PrijavaDto kreirajPrijavu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PrijavaCreateRequestDto zahtev) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return prijavaService.kreirajPrijavu(kandidatId, zahtev.terminiIds());
    }

    @GetMapping("/moje")
    public List<PrijavaDto> getMojePrijave(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return prijavaService.getMojePrijave(kandidatId);
    }

    @PutMapping("/{id}/otkazi")
    public PrijavaDto otkaziPrijavu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return prijavaService.otkaziPrijavu(kandidatId, id);
    }

    @GetMapping
    public List<PrijavaDto> pretraziPrijave(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String ime,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long tipIspitaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumTermina,
            @RequestParam(required = false) String status) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return prijavaService.pretraziPrijave(ime, email, tipIspitaId, datumTermina, status);
    }

    @GetMapping("/{id}")
    public PrijavaDto getPrijavaDetalj(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return prijavaService.getPrijavaDetalj(id);
    }

    @GetMapping("/statistika")
    public PrijavaStatistikaDto getStatistika(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return prijavaService.getStatistikaPrihoda();
    }
}

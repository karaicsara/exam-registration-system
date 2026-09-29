package org.example.backend.controller;

import org.example.backend.dto.MestoOdrzavanjaDto;
import org.example.backend.dto.MestoOdrzavanjaZahtevDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.AdministratorService;
import org.example.backend.service.MestoOdrzavanjaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesta-odrzavanja")
@RequiredArgsConstructor
public class MestoOdrzavanjaController {

    private final MestoOdrzavanjaService mestoOdrzavanjaService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<MestoOdrzavanjaDto> getSvaMesta() {
        return mestoOdrzavanjaService.getSvaMesta();
    }

    @PostMapping
    public MestoOdrzavanjaDto kreirajMesto(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody MestoOdrzavanjaZahtevDto zahtev) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return mestoOdrzavanjaService.kreirajMesto(zahtev);
    }

    @PutMapping("/{id}")
    public MestoOdrzavanjaDto izmeniMesto(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody MestoOdrzavanjaZahtevDto zahtev) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return mestoOdrzavanjaService.izmeniMesto(id, zahtev);
    }

    @DeleteMapping("/{id}")
    public void obrisiMesto(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        mestoOdrzavanjaService.obrisiMesto(id);
    }
}

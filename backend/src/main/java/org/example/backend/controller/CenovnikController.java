package org.example.backend.controller;

import org.example.backend.dto.CenovnikDto;
import org.example.backend.dto.CenovnikZahtevDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.AdministratorService;
import org.example.backend.service.CenovnikService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cenovnici")
@RequiredArgsConstructor
public class CenovnikController {

    private final CenovnikService cenovnikService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<CenovnikDto> getAktivneCene() {
        return cenovnikService.getAktivneCene();
    }

    @GetMapping("/istorija/{tipIspitaId}")
    public List<CenovnikDto> getIstorija(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long tipIspitaId) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return cenovnikService.getIstorijaZaTip(tipIspitaId);
    }

    @PostMapping
    public CenovnikDto kreirajNovuCenu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CenovnikZahtevDto zahtev) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return cenovnikService.kreirajNovuCenu(zahtev);
    }
}

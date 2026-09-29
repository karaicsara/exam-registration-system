package org.example.backend.controller;

import org.example.backend.dto.OdbijanjeUplateDto;
import org.example.backend.dto.UplataDto;
import org.example.backend.dto.UplataStatistikaDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.AdministratorService;
import org.example.backend.service.KandidatService;
import org.example.backend.service.UplataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/uplate")
@RequiredArgsConstructor
public class UplataController {

    private final UplataService uplataService;
    private final JwtUtil jwtUtil;

    @PostMapping(value = "/prijava/{prijavaId}", consumes = "multipart/form-data")
    public UplataDto posaljiDokaz(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long prijavaId,
            @RequestParam("fajl") MultipartFile fajl) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return uplataService.posaljiDokazUplate(kandidatId, prijavaId, fajl);
    }

    @GetMapping("/prijava/{prijavaId}")
    public List<UplataDto> getUplateZaPrijavu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long prijavaId) {
        Long kandidatId = jwtUtil.tryGetUserId(authHeader, KandidatService.TIP_KORISNIKA);
        if (kandidatId == null) {
            jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        }
        return uplataService.getUplateZaPrijavu(prijavaId, kandidatId);
    }

    @PutMapping("/{id}/odobri")
    public UplataDto odobriUplatu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        Long adminId = jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return uplataService.odobriUplatu(adminId, id);
    }

    @PutMapping("/{id}/odbij")
    public UplataDto odbijUplatu(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody OdbijanjeUplateDto zahtev) {
        Long adminId = jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return uplataService.odbijUplatu(adminId, id, zahtev.razlog());
    }

    @GetMapping("/statistika")
    public UplataStatistikaDto getStatistika(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return uplataService.getStatistikaUplata();
    }
}

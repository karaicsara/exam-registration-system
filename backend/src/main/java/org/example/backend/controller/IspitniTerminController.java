package org.example.backend.controller;

import org.example.backend.dto.IspitniTerminDto;
import org.example.backend.dto.IspitniTerminZahtevDto;
import org.example.backend.dto.KandidatZaTerminDto;
import org.example.backend.dto.TerminDetaljDto;
import org.example.backend.dto.TerminPdfDto;
import org.example.backend.dto.TerminStatistikaDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.AdministratorService;
import org.example.backend.service.IspitniTerminService;
import org.example.backend.service.KandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ispitni-termini")
@RequiredArgsConstructor
public class IspitniTerminController {

    private final IspitniTerminService ispitniTerminService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<IspitniTerminDto> getSviTermini(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Long tipIspitaId) {
        Long kandidatId = jwtUtil.tryGetUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return ispitniTerminService.getSviTermini(tipIspitaId, kandidatId);
    }

    @PostMapping
    public IspitniTerminDto kreirajTermin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody IspitniTerminZahtevDto zahtev) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return ispitniTerminService.kreirajTermin(zahtev);
    }

    @PutMapping("/{id}")
    public IspitniTerminDto izmeniTermin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody IspitniTerminZahtevDto zahtev) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return ispitniTerminService.izmeniTermin(id, zahtev);
    }

    @DeleteMapping("/{id}")
    public void obrisiTermin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        ispitniTerminService.obrisiTermin(id);
    }

    @GetMapping("/{id}/kandidati")
    public List<KandidatZaTerminDto> getKandidatiZaTermin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return ispitniTerminService.getKandidatiZaTermin(id);
    }

    @GetMapping("/{id}/detalji")
    public TerminDetaljDto getDetaljiTermina(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return ispitniTerminService.getDetaljiTermina(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> preuzmiPdfSpiska(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        TerminPdfDto pdf = ispitniTerminService.generisiPdfSpiska(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pdf.nazivFajla() + "\"")
                .body(pdf.sadrzaj());
    }

    @GetMapping("/statistika")
    public TerminStatistikaDto getStatistika(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        jwtUtil.requireUserId(authHeader, AdministratorService.TIP_KORISNIKA);
        return ispitniTerminService.getStatistikaTermina();
    }
}

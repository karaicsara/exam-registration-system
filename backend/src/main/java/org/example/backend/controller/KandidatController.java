package org.example.backend.controller;

import org.example.backend.dto.KandidatIzmenaDto;
import org.example.backend.dto.KandidatProfilDto;
import org.example.backend.dto.LoginRequestDto;
import org.example.backend.dto.LoginResponseDto;
import org.example.backend.dto.MessageResponseDto;
import org.example.backend.dto.PonovnoSlanjeKodaDto;
import org.example.backend.dto.RegistracijaKandidataDto;
import org.example.backend.dto.VerifikacijaKodaDto;
import org.example.backend.security.JwtUtil;
import org.example.backend.service.KandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kandidati")
@RequiredArgsConstructor
public class KandidatController {

    private final KandidatService kandidatService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto zahtev) {
        return kandidatService.login(zahtev);
    }

    @PostMapping("/registracija/pocetak")
    public MessageResponseDto zapocniRegistraciju(@RequestBody RegistracijaKandidataDto zahtev) {
        return kandidatService.zapocniRegistraciju(zahtev);
    }

    @PostMapping("/registracija/ponovo-posalji-kod")
    public MessageResponseDto ponovoPosaljiKod(@RequestBody PonovnoSlanjeKodaDto zahtev) {
        return kandidatService.ponovoPosaljiKod(zahtev.email());
    }

    @PostMapping("/registracija/verifikacija")
    public LoginResponseDto verifikujKod(@RequestBody VerifikacijaKodaDto zahtev) {
        return kandidatService.verifikujKodIZavrsiRegistraciju(zahtev);
    }

    @GetMapping("/me")
    public KandidatProfilDto getMojiPodaci(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return kandidatService.getMojiPodaci(kandidatId);
    }

    @PutMapping("/me")
    public KandidatProfilDto azurirajMojePodatke(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody KandidatIzmenaDto zahtev) {
        Long kandidatId = jwtUtil.requireUserId(authHeader, KandidatService.TIP_KORISNIKA);
        return kandidatService.azurirajMojePodatke(kandidatId, zahtev);
    }
}

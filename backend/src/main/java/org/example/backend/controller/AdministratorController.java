package org.example.backend.controller;

import org.example.backend.dto.LoginRequestDto;
import org.example.backend.dto.LoginResponseDto;
import org.example.backend.service.AdministratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/administratori")
@RequiredArgsConstructor
public class AdministratorController {

    private final AdministratorService administratorService;

    // Namerno ne postoji endpoint za registraciju administratora - nalozi se unose SQL skriptom.
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto zahtev) {
        return administratorService.login(zahtev);
    }
}

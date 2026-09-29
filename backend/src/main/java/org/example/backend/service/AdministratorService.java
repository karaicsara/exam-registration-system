package org.example.backend.service;

import org.example.backend.dto.LoginRequestDto;
import org.example.backend.dto.LoginResponseDto;
import org.example.backend.entity.Administrator;
import org.example.backend.repository.AdministratorRepository;
import org.example.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdministratorService {

    public static final String TIP_KORISNIKA = "ADMINISTRATOR";

    private final AdministratorRepository administratorRepository;
    private final JwtUtil jwtUtil;

    // Administratorski nalozi se ne registruju kroz aplikaciju - unose se unapred SQL skriptom,
    // zato ovde ne postoji metoda za registraciju niti hesiranje lozinke (skolski projekat).
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto zahtev) {
        Administrator administrator = administratorRepository.findByUsername(zahtev.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešno korisničko ime ili lozinka"));

        if (!administrator.getPassword().equals(zahtev.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešno korisničko ime ili lozinka");
        }

        String token = jwtUtil.generateToken(administrator.getId(), administrator.getUsername(), administrator.getIme(), administrator.getPrezime(), TIP_KORISNIKA);
        return new LoginResponseDto(administrator.getId(), administrator.getIme(), administrator.getPrezime(), administrator.getUsername(), administrator.getEmail(), TIP_KORISNIKA, token);
    }

    public Administrator pronadjiAdministratora(Long administratorId) {
        return administratorRepository.findById(administratorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrator nije pronađen"));
    }
}

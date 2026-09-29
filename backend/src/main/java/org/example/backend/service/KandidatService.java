package org.example.backend.service;

import org.example.backend.dto.KandidatIzmenaDto;
import org.example.backend.dto.KandidatProfilDto;
import org.example.backend.dto.LoginRequestDto;
import org.example.backend.dto.LoginResponseDto;
import org.example.backend.dto.MessageResponseDto;
import org.example.backend.dto.RegistracijaKandidataDto;
import org.example.backend.dto.VerifikacijaKodaDto;
import org.example.backend.entity.Kandidat;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.KandidatRepository;
import org.example.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KandidatService {

    public static final String TIP_KORISNIKA = "KANDIDAT";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MIN_DUZINA_LOZINKE = 6;
    private static final int DUZINA_KODA = 6;
    private static final long KOD_ISTICE_MINUTA = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KandidatRepository kandidatRepository;
    private final JwtUtil jwtUtil;
    private final AsyncMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailSenderAddress;

    // Kandidat entitet ostaje potpuno nepromenjen sve dok verifikacija mejla ne prodje -
    // nezavrsena registracija se drzi samo u memoriji servisa, ne u bazi.
    private final Map<String, PendingRegistracija> naCekanjuVerifikacije = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto zahtev) {
        Kandidat kandidat = kandidatRepository.findByUsername(zahtev.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešno korisničko ime ili lozinka"));

        if (!kandidat.getPassword().equals(zahtev.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pogrešno korisničko ime ili lozinka");
        }

        return toLoginResponse(kandidat);
    }

    public MessageResponseDto zapocniRegistraciju(RegistracijaKandidataDto dto) {
        validirajRegistraciju(dto);

        String email = normalizuj(dto.email());
        String username = dto.username().trim();

        if (kandidatRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kandidat sa ovim email-om je već registrovan");
        }
        if (kandidatRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Korisničko ime je već zauzeto");
        }

        String kod = generisiKod();
        naCekanjuVerifikacije.put(email, new PendingRegistracija(
                dto.ime().trim(),
                dto.prezime().trim(),
                dto.telefon(),
                username,
                dto.password(),
                email,
                kod,
                LocalDateTime.now().plusMinutes(KOD_ISTICE_MINUTA)
        ));

        posaljiKodEmail(email, kod);

        return new MessageResponseDto("Verifikacioni kod je poslat na email " + email);
    }

    public MessageResponseDto ponovoPosaljiKod(String email) {
        String normalizovan = normalizuj(email);
        PendingRegistracija pending = naCekanjuVerifikacije.get(normalizovan);
        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registracija za ovaj email nije započeta");
        }

        String noviKod = generisiKod();
        naCekanjuVerifikacije.put(normalizovan, new PendingRegistracija(
                pending.ime(), pending.prezime(), pending.telefon(), pending.username(), pending.password(),
                normalizovan, noviKod, LocalDateTime.now().plusMinutes(KOD_ISTICE_MINUTA)
        ));
        posaljiKodEmail(normalizovan, noviKod);

        return new MessageResponseDto("Novi verifikacioni kod je poslat na email " + normalizovan);
    }

    @Transactional
    public LoginResponseDto verifikujKodIZavrsiRegistraciju(VerifikacijaKodaDto dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new ResponseStatu sException(HttpStatus.BAD_REQUEST, "Email je obavezan");
        }
        String email = normalizuj(dto.email());

        PendingRegistracija pending = naCekanjuVerifikacije.get(email);
        if (pending == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registracija za ovaj email nije započeta");
        }
        if (pending.istice().isBefore(LocalDateTime.now())) {
            naCekanjuVerifikacije.remove(email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifikacioni kod je istekao, pokrenite registraciju ponovo");
        }
        if (dto.kod() == null || !pending.kod().equals(dto.kod().trim())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Verifikacioni kod nije tačan");
        }
        if (kandidatRepository.existsByEmail(email)) {
            naCekanjuVerifikacije.remove(email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kandidat sa ovim email-om je u međuvremenu registrovan");
        }

        Kandidat kandidat = new Kandidat();
        kandidat.setIme(pending.ime());
        kandidat.setPrezime(pending.prezime());
        kandidat.setTelefon(pending.telefon());
        kandidat.setUsername(pending.username());
        kandidat.setPassword(pending.password());
        kandidat.setEmail(email);
        kandidat = kandidatRepository.save(kandidat);

        naCekanjuVerifikacije.remove(email);

        return toLoginResponse(kandidat);
    }

    @Transactional(readOnly = true)
    public KandidatProfilDto getMojiPodaci(Long kandidatId) {
        return toProfilDto(pronadjiKandidata(kandidatId));
    }

    @Transactional
    public KandidatProfilDto azurirajMojePodatke(Long kandidatId, KandidatIzmenaDto dto) {
        Kandidat kandidat = pronadjiKandidata(kandidatId);

        if (dto.ime() == null || dto.ime().isBlank() || dto.prezime() == null || dto.prezime().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ime i prezime su obavezni");
        }

        kandidat.setIme(dto.ime().trim());
        kandidat.setPrezime(dto.prezime().trim());
        kandidat.setTelefon(dto.telefon());
        kandidat = kandidatRepository.save(kandidat);

        return toProfilDto(kandidat);
    }

    public Kandidat pronadjiKandidata(Long kandidatId) {
        return kandidatRepository.findById(kandidatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kandidat nije pronađen"));
    }

    private void validirajRegistraciju(RegistracijaKandidataDto dto) {
        if (dto.ime() == null || dto.ime().isBlank() || dto.prezime() == null || dto.prezime().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ime i prezime su obavezni");
        }
        if (dto.username() == null || dto.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Korisničko ime je obavezno");
        }
        if (dto.password() == null || dto.password().length() < MIN_DUZINA_LOZINKE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lozinka mora imati bar " + MIN_DUZINA_LOZINKE + " karaktera");
        }
        if (dto.email() == null || !EMAIL_PATTERN.matcher(dto.email()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email nije validan");
        }
    }

    private String generisiKod() {
        int broj = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%0" + DUZINA_KODA + "d", broj);
    }

    private void posaljiKodEmail(String email, String kod) {
        String telo = "Poštovani,\n\n" +
                "Vaš verifikacioni kod za registraciju na sistem za prijavu probnog prijemnog ispita je: " + kod + "\n\n" +
                "Kod važi " + KOD_ISTICE_MINUTA + " minuta. Unesite ga u aplikaciji kako biste završili registraciju.\n\n" +
                "Srdačan pozdrav,\nFakultet organizacionih nauka";

        // Slanje je asinhrono (AsyncMailSender#posaljiAsinhrono je @Async) - registracija se ne
        // blokira čekanjem SMTP odgovora, a eventualni neuspeh slanja se samo loguje.
        mailSender.posaljiAsinhrono(mailSenderAddress, email, "Verifikacija email adrese - Probni prijemni ispit FON", telo);
    }

    private String normalizuj(String email) {
        return email.trim().toLowerCase();
    }

    private LoginResponseDto toLoginResponse(Kandidat kandidat) {
        String token = jwtUtil.generateToken(kandidat.getId(), kandidat.getUsername(), kandidat.getIme(), kandidat.getPrezime(), TIP_KORISNIKA);
        return new LoginResponseDto(kandidat.getId(), kandidat.getIme(), kandidat.getPrezime(), kandidat.getUsername(), kandidat.getEmail(), TIP_KORISNIKA, token);
    }

    private KandidatProfilDto toProfilDto(Kandidat kandidat) {
        return new KandidatProfilDto(kandidat.getId(), kandidat.getIme(), kandidat.getPrezime(), kandidat.getTelefon(), kandidat.getUsername(), kandidat.getEmail());
    }

    private record PendingRegistracija(String ime, String prezime, String telefon, String username, String password,
                                        String email, String kod, LocalDateTime istice) {
    }
}

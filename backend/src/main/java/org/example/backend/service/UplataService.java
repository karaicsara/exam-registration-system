package org.example.backend.service;

import org.example.backend.dto.UplataDto;
import org.example.backend.dto.UplataStatistikaDto;
import org.example.backend.entity.Administrator;
import org.example.backend.entity.Prijava;
import org.example.backend.entity.Uplata;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.AdministratorRepository;
import org.example.backend.repository.PrijavaRepository;
import org.example.backend.repository.UplataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UplataService {

    public static final String STATUS_NA_CEKANJU = "NA_CEKANJU";
    public static final String STATUS_ODOBRENA = "ODOBRENA";
    public static final String STATUS_ODBIJENA = "ODBIJENA";

    private static final List<String> DOZVOLJENI_TIPOVI = List.of("image/jpeg", "image/png", "application/pdf");

    private final UplataRepository uplataRepository;
    private final PrijavaRepository prijavaRepository;
    private final AdministratorRepository administratorRepository;
    private final PrijavaService prijavaService;
    private final AsyncMailSender mailSender;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${spring.mail.username:}")
    private String mailSenderAddress;

    @Transactional
    public UplataDto posaljiDokazUplate(Long kandidatId, Long prijavaId, MultipartFile fajl) {
        Prijava prijava = prijavaRepository.findById(prijavaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prijava nije pronađena"));

        if (!prijava.getKandidat().getId().equals(kandidatId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ova prijava ne pripada ulogovanom kandidatu");
        }
        if (!prijavaService.mozeSeDostaviUplata(prijava)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Za ovu prijavu više nije moguće dostaviti dokaz o uplati");
        }
        if (fajl == null || fajl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dokaz o uplati je obavezan");
        }
        if (!DOZVOLJENI_TIPOVI.contains(fajl.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dozvoljeni su samo fajlovi tipa JPG, PNG ili PDF");
        }

        String putanja = sacuvajFajl(fajl);

        Uplata uplata = new Uplata();
        uplata.setPutanjaDokumenta(putanja);
        uplata.setDatumSlanja(LocalDate.now());
        uplata.setStatus(STATUS_NA_CEKANJU);
        uplata.setNapomena("Čeka se provera administratora");
        uplata.setPrijava(prijava);
        uplata = uplataRepository.save(uplata);

        return toDto(uplata);
    }

    @Transactional(readOnly = true)
    public List<UplataDto> getUplateZaPrijavu(Long prijavaId, Long kandidatId) {
        Prijava prijava = prijavaRepository.findById(prijavaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prijava nije pronađena"));

        if (kandidatId != null && !prijava.getKandidat().getId().equals(kandidatId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ova prijava ne pripada ulogovanom kandidatu");
        }

        return uplataRepository.findByPrijavaIdOrderByDatumSlanjaDesc(prijavaId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UplataDto odobriUplatu(Long adminId, Long uplataId) {
        Administrator administrator = pronadjiAdministratora(adminId);
        Uplata uplata = pronadjiUplatu(uplataId);

        if (!STATUS_NA_CEKANJU.equals(uplata.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ova uplata je već obrađena");
        }

        uplata.setStatus(STATUS_ODOBRENA);
        uplata.setNapomena("Odobrio administrator " + administrator.getIme() + " " + administrator.getPrezime());
        uplata = uplataRepository.save(uplata);

        Prijava prijava = uplata.getPrijava();
        prijava.setStatus(PrijavaService.STATUS_PRIJAVLJENA);
        prijavaRepository.save(prijava);

        posalji(prijava.getKandidat().getEmail(), "Uplata odobrena - Probni prijemni ispit",
                "Poštovani/a " + prijava.getKandidat().getIme() + ",\n\nVaša uplata je evidentirana i odobrena. Vaša prijava za probni prijemni ispit ima status PRIJAVLJEN.\n\nSrdačan pozdrav,\nFakultet organizacionih nauka");

        return toDto(uplata);
    }

    @Transactional
    public UplataDto odbijUplatu(Long adminId, Long uplataId, String razlog) {
        pronadjiAdministratora(adminId);
        if (razlog == null || razlog.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razlog odbijanja je obavezan");
        }

        Uplata uplata = pronadjiUplatu(uplataId);
        if (!STATUS_NA_CEKANJU.equals(uplata.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ova uplata je već obrađena");
        }

        uplata.setStatus(STATUS_ODBIJENA);
        uplata.setNapomena(razlog);
        uplata = uplataRepository.save(uplata);

        Prijava prijava = uplata.getPrijava();
        posalji(prijava.getKandidat().getEmail(), "Uplata odbijena - Probni prijemni ispit",
                "Poštovani/a " + prijava.getKandidat().getIme() + ",\n\nVaš dokaz o uplati je odbijen iz sledećeg razloga: " + razlog +
                        "\n\nMožete dostaviti novi dokaz o uplati kroz aplikaciju.\n\nSrdačan pozdrav,\nFakultet organizacionih nauka");

        return toDto(uplata);
    }

    @Transactional(readOnly = true)
    public UplataStatistikaDto getStatistikaUplata() {
        Map<String, Long> brojPoStatusu = uplataRepository.findAll().stream()
                .collect(Collectors.groupingBy(Uplata::getStatus, Collectors.counting()));

        long naCekanju = brojPoStatusu.getOrDefault(STATUS_NA_CEKANJU, 0L);
        long odobrene = brojPoStatusu.getOrDefault(STATUS_ODOBRENA, 0L);
        long odbijene = brojPoStatusu.getOrDefault(STATUS_ODBIJENA, 0L);
        long odluceno = odobrene + odbijene;
        double procenatOdobrenih = odluceno == 0 ? 0 : (odobrene * 100.0) / odluceno;

        return new UplataStatistikaDto(naCekanju, odobrene, odbijene, procenatOdobrenih);
    }

    private Uplata pronadjiUplatu(Long uplataId) {
        return uplataRepository.findById(uplataId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uplata nije pronađena"));
    }

    private Administrator pronadjiAdministratora(Long adminId) {
        return administratorRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrator nije pronađen"));
    }

    private String sacuvajFajl(MultipartFile fajl) {
        try {
            Path direktorijum = Path.of(uploadDir);
            Files.createDirectories(direktorijum);

            String originalno = fajl.getOriginalFilename() != null ? fajl.getOriginalFilename() : "dokaz";
            String ekstenzija = originalno.contains(".") ? originalno.substring(originalno.lastIndexOf('.')) : "";
            String imeFajla = UUID.randomUUID() + ekstenzija;

            Path putanja = direktorijum.resolve(imeFajla);
            fajl.transferTo(putanja);

            return "/uploads/uplate/" + imeFajla;
        } catch (IOException exception) {
            throw new UncheckedIOException("Dokaz o uplati nije mogao biti sačuvan", exception);
        }
    }

    private void posalji(String prima, String naslov, String telo) {
        // Asinhrono (AsyncMailSender#posaljiAsinhrono je @Async) - status uplate/prijave ostaje
        // na snazi odmah, bez čekanja na SMTP odgovor ili ishod slanja emaila.
        mailSender.posaljiAsinhrono(mailSenderAddress, prima, naslov, telo);
    }

    private UplataDto toDto(Uplata uplata) {
        return new UplataDto(uplata.getId(), uplata.getPrijava().getId(), uplata.getPutanjaDokumenta(),
                uplata.getDatumSlanja(), uplata.getStatus(), uplata.getNapomena());
    }
}

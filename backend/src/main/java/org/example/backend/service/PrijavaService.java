package org.example.backend.service;

import org.example.backend.dto.PrihodOblastiDto;
import org.example.backend.dto.PrijavaDto;
import org.example.backend.dto.PrijavaStatistikaDto;
import org.example.backend.dto.StavkaPrijaveDto;
import org.example.backend.dto.UplataDto;
import org.example.backend.entity.IspitniTermin;
import org.example.backend.entity.Kandidat;
import org.example.backend.entity.Prijava;
import org.example.backend.entity.StavkaPrijave;
import org.example.backend.entity.Uplata;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.IspitniTerminRepository;
import org.example.backend.repository.KandidatRepository;
import org.example.backend.repository.PrijavaRepository;
import org.example.backend.repository.StavkaPrijaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrijavaService {

    public static final String STATUS_KREIRANA = "KREIRANA";
    public static final String STATUS_PRIJAVLJENA = "PRIJAVLJENA";
    public static final String STATUS_OTKAZANA = "OTKAZANA";

    private static final double POPUST_KOMBINOVANA_PRIJAVA = 0.10;
    private static final double POPUST_ISTI_TIP_VISE_TERMINA = 0.05;
    private static final double POPUST_VISE_TERMINA_UKUPNO = 0.05;

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    private final PrijavaRepository prijavaRepository;
    private final KandidatRepository kandidatRepository;
    private final IspitniTerminRepository ispitniTerminRepository;
    private final StavkaPrijaveRepository stavkaPrijaveRepository;
    private final IspitniTerminService ispitniTerminService;
    private final AsyncMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailSenderAddress;

    @Transactional
    public PrijavaDto kreirajPrijavu(Long kandidatId, List<Long> terminiIds) {
        if (terminiIds == null || terminiIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Potrebno je izabrati bar jedan termin");
        }

        Kandidat kandidat = kandidatRepository.findById(kandidatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kandidat nije pronađen"));

        List<IspitniTermin> termini = ispitniTerminRepository.findAllByIdIn(terminiIds);
        if (termini.size() != Set.copyOf(terminiIds).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jedan ili više izabranih termina ne postoji");
        }

        LocalDate danas = LocalDate.now();
        for (IspitniTermin termin : termini) {
            if (!Boolean.TRUE.equals(termin.getDostupan()) || termin.getDatumOdrzavanja().isBefore(danas)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Termin " + termin.getTipIspita().getNaziv()
                        + " (" + termin.getDatumOdrzavanja().format(DATUM_FORMAT) + ") trenutno nije dostupan za prijavu");
            }
        }

        Set<Long> aktivniTerminiKandidata = prijavaRepository.findByKandidatIdOrderByDatumKreiranjaDesc(kandidatId).stream()
                .filter(p -> !STATUS_OTKAZANA.equals(p.getStatus()))
                .flatMap(p -> p.getStavkePrijave().stream())
                .map(stavka -> stavka.getIspitniTermin().getId())
                .collect(Collectors.toSet());
        boolean vecPrijavljen = termini.stream().anyMatch(t -> aktivniTerminiKandidata.contains(t.getId()));
        if (vecPrijavljen) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Već imate aktivnu prijavu za jedan od izabranih termina");
        }

        Prijava prijava = new Prijava();
        prijava.setDatumKreiranja(danas);
        prijava.setStatus(STATUS_KREIRANA);
        prijava.setKandidat(kandidat);
        // popust/ukupanIznos moraju imati vrednost već pri prvom čuvanju (kolone su NOT NULL) -
        // konačne vrednosti se izračunavaju i upisuju u drugom save() nakon što se znaju stavke.
        prijava.setPopust(0.0);
        prijava.setUkupanIznos(0.0);
        prijava = prijavaRepository.save(prijava);

        double bruto = 0;
        for (IspitniTermin termin : termini) {
            double cena = ispitniTerminService.izracunajTrenutnuCenu(termin);
            bruto += cena;

            StavkaPrijave stavka = new StavkaPrijave();
            stavka.setIspitniTermin(termin);
            stavka.setPrijava(prijava);
            stavka.setCenaNaDanPrijave(cena);
            stavkaPrijaveRepository.save(stavka);
        }

        double popust = bruto * izracunajStopuPopusta(termini);
        prijava.setPopust(popust);
        prijava.setUkupanIznos(bruto - popust);
        prijava = prijavaRepository.save(prijava);

        posaljiPotvrduPrijave(prijava);

        return toDto(prijava);
    }

    @Transactional(readOnly = true)
    public List<PrijavaDto> getMojePrijave(Long kandidatId) {
        return prijavaRepository.findByKandidatIdOrderByDatumKreiranjaDesc(kandidatId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PrijavaDto otkaziPrijavu(Long kandidatId, Long prijavaId) {
        Prijava prijava = pronadjiPrijavu(prijavaId);

        if (!prijava.getKandidat().getId().equals(kandidatId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ova prijava ne pripada ulogovanom kandidatu");
        }
        if (!mozeSeOtkazati(prijava)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prijava se ne može otkazati jer je uplata već evidentirana ili je već otkazana");
        }

        prijava.setStatus(STATUS_OTKAZANA);
        prijava = prijavaRepository.save(prijava);

        posaljiObavestenjeOtkazivanja(prijava);

        return toDto(prijava);
    }

    @Transactional(readOnly = true)
    public List<PrijavaDto> pretraziPrijave(String ime, String email, Long tipIspitaId, LocalDate datumTermina, String status) {
        String imePretraga = normalizuj(ime);
        String emailPretraga = normalizuj(email);

        return prijavaRepository.findAll().stream()
                .filter(p -> imePretraga.isEmpty()
                        || normalizuj(p.getKandidat().getIme()).contains(imePretraga)
                        || normalizuj(p.getKandidat().getPrezime()).contains(imePretraga))
                .filter(p -> emailPretraga.isEmpty() || normalizuj(p.getKandidat().getEmail()).contains(emailPretraga))
                .filter(p -> tipIspitaId == null || p.getStavkePrijave().stream()
                        .anyMatch(s -> s.getIspitniTermin().getTipIspita().getId().equals(tipIspitaId)))
                .filter(p -> datumTermina == null || p.getStavkePrijave().stream()
                        .anyMatch(s -> s.getIspitniTermin().getDatumOdrzavanja().isEqual(datumTermina)))
                .filter(p -> status == null || status.isBlank() || status.equals(p.getStatus()))
                .sorted(Comparator.comparing(Prijava::getDatumKreiranja).reversed())
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrijavaDto getPrijavaDetalj(Long prijavaId) {
        return toDto(pronadjiPrijavu(prijavaId));
    }

    @Transactional(readOnly = true)
    public PrijavaStatistikaDto getStatistikaPrihoda() {
        List<Prijava> svePrijave = prijavaRepository.findAll();

        Map<String, double[]> poOblasti = new LinkedHashMap<>();
        for (Prijava prijava : svePrijave) {
            if (STATUS_OTKAZANA.equals(prijava.getStatus())) {
                continue;
            }
            Map<String, Double> brutoPoOblastiZaPrijavu = prijava.getStavkePrijave().stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getIspitniTermin().getTipIspita().getNaziv(),
                            Collectors.summingDouble(StavkaPrijave::getCenaNaDanPrijave)));

            double ukupnoBrutoPrijave = brutoPoOblastiZaPrijavu.values().stream().mapToDouble(Double::doubleValue).sum();
            double popust = prijava.getPopust() == null ? 0 : prijava.getPopust();

            for (Map.Entry<String, Double> entry : brutoPoOblastiZaPrijavu.entrySet()) {
                double udeoPopusta = ukupnoBrutoPrijave == 0 ? 0 : popust * (entry.getValue() / ukupnoBrutoPrijave);
                double[] vrednosti = poOblasti.computeIfAbsent(entry.getKey(), k -> new double[2]);
                vrednosti[0] += entry.getValue();
                vrednosti[1] += udeoPopusta;
            }
        }

        List<PrihodOblastiDto> prihodPoOblasti = poOblasti.entrySet().stream()
                .map(e -> new PrihodOblastiDto(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[0] - e.getValue()[1]))
                .toList();

        Map<String, Long> brojPoStatusu = svePrijave.stream()
                .collect(Collectors.groupingBy(Prijava::getStatus, Collectors.counting()));

        return new PrijavaStatistikaDto(
                prihodPoOblasti,
                brojPoStatusu.getOrDefault(STATUS_KREIRANA, 0L),
                brojPoStatusu.getOrDefault(STATUS_PRIJAVLJENA, 0L),
                brojPoStatusu.getOrDefault(STATUS_OTKAZANA, 0L)
        );
    }

    public Prijava pronadjiPrijavu(Long prijavaId) {
        return prijavaRepository.findById(prijavaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prijava nije pronađena"));
    }

    public boolean mozeSeOtkazati(Prijava prijava) {
        return STATUS_KREIRANA.equals(prijava.getStatus());
    }

    public boolean mozeSeDostaviUplata(Prijava prijava) {
        return STATUS_KREIRANA.equals(prijava.getStatus());
    }

    private String normalizuj(String vrednost) {
        return vrednost == null ? "" : vrednost.trim().toLowerCase();
    }

    private double izracunajStopuPopusta(List<IspitniTermin> termini) {
        double stopa = 0;

        long brojTipovaIspita = termini.stream().map(t -> t.getTipIspita().getId()).distinct().count();
        if (brojTipovaIspita >= 2) {
            stopa += POPUST_KOMBINOVANA_PRIJAVA;
        }

        Map<Long, Long> brojPoTipu = termini.stream()
                .collect(Collectors.groupingBy(t -> t.getTipIspita().getId(), Collectors.counting()));
        if (brojPoTipu.values().stream().anyMatch(broj -> broj >= 2)) {
            stopa += POPUST_ISTI_TIP_VISE_TERMINA;
        }

        if (termini.size() >= 3) {
            stopa += POPUST_VISE_TERMINA_UKUPNO;
        }

        return stopa;
    }

    private void posaljiPotvrduPrijave(Prijava prijava) {
        StringBuilder telo = new StringBuilder();
        telo.append("Poštovani/a ").append(prijava.getKandidat().getIme()).append(",\n\n");
        telo.append("Uspešno ste kreirali prijavu za probni prijemni ispit na Fakultetu organizacionih nauka.\n\n");
        telo.append("Izabrani termini:\n");
        for (StavkaPrijave stavka : prijava.getStavkePrijave()) {
            IspitniTermin termin = stavka.getIspitniTermin();
            telo.append("- ").append(termin.getTipIspita().getNaziv()).append(", ")
                    .append(termin.getDatumOdrzavanja().format(DATUM_FORMAT)).append(" u ").append(termin.getVremePocetka())
                    .append(", ").append(termin.getMestoOdrzavanja().getNaziv()).append(" (cena: ")
                    .append(stavka.getCenaNaDanPrijave()).append(" RSD)\n");
        }
        telo.append("\nUkupan iznos za uplatu: ").append(prijava.getUkupanIznos()).append(" RSD");
        if (prijava.getPopust() != null && prijava.getPopust() > 0) {
            telo.append(" (uključen popust od ").append(prijava.getPopust()).append(" RSD)");
        }
        telo.append("\n\nMolimo da uplatu izvršite i dostavite dokaz o uplati kroz aplikaciju kako bi vaša prijava bila evidentirana.\n\n");
        telo.append("Srdačan pozdrav,\nFakultet organizacionih nauka");

        posalji(prijava.getKandidat().getEmail(), "Potvrda kreirane prijave - Probni prijemni ispit", telo.toString());
    }

    private void posaljiObavestenjeOtkazivanja(Prijava prijava) {
        String telo = "Poštovani/a " + prijava.getKandidat().getIme() + ",\n\n" +
                "Vaša prijava za probni prijemni ispit je otkazana na vaš zahtev.\n\n" +
                "Srdačan pozdrav,\nFakultet organizacionih nauka";

        posalji(prijava.getKandidat().getEmail(), "Prijava otkazana - Probni prijemni ispit", telo);
    }

    private void posalji(String prima, String naslov, String telo) {
        // Asinhrono (AsyncMailSender#posaljiAsinhrono je @Async) - prijava ostaje evidentirana
        // odmah, bez čekanja na SMTP odgovor ili ishod slanja emaila.
        mailSender.posaljiAsinhrono(mailSenderAddress, prima, naslov, telo);
    }

    public PrijavaDto toDto(Prijava prijava) {
        List<StavkaPrijaveDto> stavkeDto = prijava.getStavkePrijave().stream()
                .map(s -> new StavkaPrijaveDto(
                        s.getId(),
                        s.getIspitniTermin().getId(),
                        s.getIspitniTermin().getTipIspita().getNaziv(),
                        s.getIspitniTermin().getDatumOdrzavanja(),
                        s.getIspitniTermin().getVremePocetka(),
                        s.getIspitniTermin().getMestoOdrzavanja().getNaziv(),
                        s.getCenaNaDanPrijave()
                ))
                .toList();

        List<UplataDto> uplateDto = prijava.getUplate().stream()
                .sorted(Comparator.comparing(Uplata::getDatumSlanja).reversed())
                .map(u -> new UplataDto(u.getId(), prijava.getId(), u.getPutanjaDokumenta(), u.getDatumSlanja(), u.getStatus(), u.getNapomena()))
                .toList();

        double bruto = stavkeDto.stream().mapToDouble(StavkaPrijaveDto::cenaNaDanPrijave).sum();

        return new PrijavaDto(
                prijava.getId(),
                prijava.getDatumKreiranja(),
                bruto,
                prijava.getPopust(),
                prijava.getUkupanIznos(),
                prijava.getStatus(),
                prijava.getNapomena(),
                mozeSeOtkazati(prijava),
                mozeSeDostaviUplata(prijava),
                prijava.getKandidat().getId(),
                prijava.getKandidat().getIme(),
                prijava.getKandidat().getPrezime(),
                prijava.getKandidat().getEmail(),
                stavkeDto,
                uplateDto
        );
    }
}

package org.example.backend.service;

import org.example.backend.dto.IspitniTerminDto;
import org.example.backend.dto.IspitniTerminZahtevDto;
import org.example.backend.dto.KandidatUTerminuDto;
import org.example.backend.dto.KandidatZaTerminDto;
import org.example.backend.dto.TerminDetaljDto;
import org.example.backend.dto.TerminPdfDto;
import org.example.backend.dto.TerminStatistikaDto;
import org.example.backend.dto.TopTerminDto;
import org.example.backend.entity.IspitniTermin;
import org.example.backend.entity.Kandidat;
import org.example.backend.entity.MestoOdrzavanja;
import org.example.backend.entity.Prijava;
import org.example.backend.entity.StavkaPrijave;
import org.example.backend.entity.TipIspita;
import org.example.backend.entity.TipIspitaCenovnik;
import org.example.backend.entity.Uplata;
import org.example.backend.pdf.TerminPdfGenerator;
import org.example.backend.repository.IspitniTerminRepository;
import org.example.backend.repository.MestoOdrzavanjaRepository;
import org.example.backend.repository.TipIspitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IspitniTerminService {

    public static final String STATUS_OTKAZANA = "OTKAZANA";

    private static final Pattern VREME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final int BROJ_NAJTRAZENIJIH = 5;

    private final IspitniTerminRepository ispitniTerminRepository;
    private final TipIspitaRepository tipIspitaRepository;
    private final MestoOdrzavanjaRepository mestoOdrzavanjaRepository;
    private final TerminPdfGenerator terminPdfGenerator;

    @Transactional(readOnly = true)
    public List<IspitniTerminDto> getSviTermini(Long tipIspitaId, Long kandidatId) {
        List<IspitniTermin> termini = tipIspitaId != null
                ? ispitniTerminRepository.findByTipIspitaId(tipIspitaId)
                : ispitniTerminRepository.findAllByOrderByDatumOdrzavanjaAscVremePocetkaAsc();

        return termini.stream().map(termin -> toDto(termin, kandidatId)).toList();
    }

    @Transactional(readOnly = true)
    public IspitniTermin pronadjiTermin(Long id) {
        return ispitniTerminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termin nije pronađen"));
    }

    @Transactional
    public IspitniTerminDto kreirajTermin(IspitniTerminZahtevDto zahtev) {
        IspitniTermin termin = new IspitniTermin();
        primeniIzmene(termin, zahtev);
        termin = ispitniTerminRepository.save(termin);
        return toDto(termin, null);
    }

    @Transactional
    public IspitniTerminDto izmeniTermin(Long id, IspitniTerminZahtevDto zahtev) {
        IspitniTermin termin = pronadjiTermin(id);

        if (jeProsao(termin)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Termin je već prošao i ne može se menjati");
        }

        primeniIzmene(termin, zahtev);
        termin = ispitniTerminRepository.save(termin);
        return toDto(termin, null);
    }

    @Transactional
    public void obrisiTermin(Long id) {
        IspitniTermin termin = pronadjiTermin(id);

        if (!aktivneStavke(termin).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Termin ima evidentirane prijave i ne može se obrisati");
        }

        ispitniTerminRepository.delete(termin);
    }

    @Transactional(readOnly = true)
    public double izracunajTrenutnuCenu(IspitniTermin termin) {
        LocalDate datum = termin.getDatumOdrzavanja();
        return termin.getTipIspita().getTipIspitaCenovnici().stream()
                .filter(TipIspitaCenovnik::getAktivan)
                .filter(veza -> veza.getCenovnik().getAktivan())
                .filter(veza -> !veza.getCenovnik().getVaziOd().isAfter(datum))
                .filter(veza -> veza.getCenovnik().getVaziDo() == null || !veza.getCenovnik().getVaziDo().isBefore(datum))
                .map(veza -> veza.getCenovnik().getIznos())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ne postoji važeći cenovnik za tip ispita: " + termin.getTipIspita().getNaziv()));
    }

    @Transactional(readOnly = true)
    public List<KandidatZaTerminDto> getKandidatiZaTermin(Long terminId) {
        IspitniTermin termin = pronadjiTermin(terminId);

        return termin.getStavkePrijave().stream()
                .filter(stavka -> !STATUS_OTKAZANA.equals(stavka.getPrijava().getStatus()))
                .sorted(Comparator.comparing(stavka -> stavka.getPrijava().getKandidat().getPrezime()))
                .map(stavka -> new KandidatZaTerminDto(
                        stavka.getPrijava().getKandidat().getIme(),
                        stavka.getPrijava().getKandidat().getPrezime(),
                        stavka.getPrijava().getKandidat().getEmail(),
                        stavka.getPrijava().getStatus()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public TerminDetaljDto getDetaljiTermina(Long terminId) {
        IspitniTermin termin = pronadjiTermin(terminId);
        List<KandidatUTerminuDto> kandidati = izlistajKandidateZaDetalje(termin);

        return new TerminDetaljDto(
                termin.getId(),
                termin.getTipIspita().getNaziv(),
                termin.getDatumOdrzavanja(),
                termin.getVremePocetka(),
                termin.getMestoOdrzavanja().getNaziv(),
                termin.getMestoOdrzavanja().getAdresa(),
                kandidati.size(),
                kandidati
        );
    }

    @Transactional(readOnly = true)
    public TerminPdfDto generisiPdfSpiska(Long terminId) {
        IspitniTermin termin = pronadjiTermin(terminId);
        TerminDetaljDto detalji = getDetaljiTermina(terminId);
        byte[] sadrzaj = terminPdfGenerator.generisiSpisakKandidata(detalji);
        return new TerminPdfDto(generisiNazivFajla(termin), sadrzaj);
    }

    private List<KandidatUTerminuDto> izlistajKandidateZaDetalje(IspitniTermin termin) {
        // Isključujemo otkazane prijave iz istih razloga kao getKandidatiZaTermin -
        // otkazana prijava ne predstavlja aktivno prisustvo kandidata na terminu, pa se
        // kandidat time ne dupira ako je u međuvremenu ponovo kreirao prijavu za isti termin.
        List<StavkaPrijave> aktivneStavke = aktivneStavke(termin).stream()
                .sorted(Comparator
                        .comparing((StavkaPrijave s) -> s.getPrijava().getKandidat().getPrezime(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(s -> s.getPrijava().getKandidat().getIme(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<KandidatUTerminuDto> rezultat = new ArrayList<>();
        int redniBroj = 1;
        for (StavkaPrijave stavka : aktivneStavke) {
            Prijava prijava = stavka.getPrijava();
            Kandidat kandidat = prijava.getKandidat();

            String statusUplate = prijava.getUplate().stream()
                    .max(Comparator.comparing(Uplata::getDatumSlanja))
                    .map(Uplata::getStatus)
                    .orElse("Nema uplate");

            rezultat.add(new KandidatUTerminuDto(
                    redniBroj++,
                    kandidat.getIme(),
                    kandidat.getPrezime(),
                    kandidat.getUsername(),
                    kandidat.getEmail(),
                    kandidat.getTelefon(),
                    prijava.getDatumKreiranja(),
                    prijava.getStatus(),
                    statusUplate
            ));
        }
        return rezultat;
    }

    private String generisiNazivFajla(IspitniTermin termin) {
        String oblast = normalizujZaNazivFajla(termin.getTipIspita().getNaziv());
        String datum = termin.getDatumOdrzavanja().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String vreme = termin.getVremePocetka().replace(":", "-");
        return "spisak_kandidata_" + oblast + "_" + datum + "_" + vreme + ".pdf";
    }

    private String normalizujZaNazivFajla(String tekst) {
        String zamenjeno = tekst.toLowerCase()
                .replace("č", "c").replace("ć", "c").replace("š", "s").replace("ž", "z").replace("đ", "dj");
        return zamenjeno.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    @Transactional(readOnly = true)
    public TerminStatistikaDto getStatistikaTermina() {
        List<IspitniTermin> termini = ispitniTerminRepository.findAll();

        long brojDostupnih = termini.stream().filter(t -> Boolean.TRUE.equals(t.getDostupan()) && !jeProsao(t)).count();
        double procenatDostupnih = termini.isEmpty() ? 0 : (brojDostupnih * 100.0) / termini.size();

        List<TopTerminDto> najtrazeniji = termini.stream()
                .map(termin -> new TopTerminDto(
                        termin.getId(),
                        termin.getTipIspita().getNaziv(),
                        termin.getDatumOdrzavanja(),
                        termin.getMestoOdrzavanja().getNaziv(),
                        aktivneStavke(termin).size()
                ))
                .filter(dto -> dto.brojPrijava() > 0)
                .sorted(Comparator.comparingLong(TopTerminDto::brojPrijava).reversed())
                .limit(BROJ_NAJTRAZENIJIH)
                .toList();

        return new TerminStatistikaDto(najtrazeniji, procenatDostupnih, termini.size());
    }

    private List<StavkaPrijave> aktivneStavke(IspitniTermin termin) {
        return termin.getStavkePrijave().stream()
                .filter(stavka -> !STATUS_OTKAZANA.equals(stavka.getPrijava().getStatus()))
                .toList();
    }

    private void primeniIzmene(IspitniTermin termin, IspitniTerminZahtevDto zahtev) {
        if (zahtev.datumOdrzavanja() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datum termina je obavezan");
        }
        if (zahtev.datumOdrzavanja().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datum termina ne sme biti u prošlosti");
        }
        if (zahtev.vremePocetka() == null || !VREME_PATTERN.matcher(zahtev.vremePocetka()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vreme početka mora biti u formatu SS:mm");
        }
        if (zahtev.tipIspitaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tip ispita mora biti izabran");
        }
        if (zahtev.mestoOdrzavanjaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesto održavanja mora biti izabrano");
        }

        TipIspita tipIspita = tipIspitaRepository.findById(zahtev.tipIspitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izabrani tip ispita ne postoji"));
        MestoOdrzavanja mesto = mestoOdrzavanjaRepository.findById(zahtev.mestoOdrzavanjaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izabrano mesto održavanja ne postoji"));

        termin.setDatumOdrzavanja(zahtev.datumOdrzavanja());
        termin.setVremePocetka(zahtev.vremePocetka());
        termin.setNapomena(zahtev.napomena());
        termin.setDostupan(zahtev.dostupan() == null || zahtev.dostupan());
        termin.setTipIspita(tipIspita);
        termin.setMestoOdrzavanja(mesto);
    }

    private boolean jeProsao(IspitniTermin termin) {
        return termin.getDatumOdrzavanja().isBefore(LocalDate.now());
    }

    private IspitniTerminDto toDto(IspitniTermin termin, Long kandidatId) {
        List<StavkaPrijave> aktivne = aktivneStavke(termin);

        boolean vecIzabranOdKandidata = kandidatId != null && aktivne.stream()
                .anyMatch(stavka -> stavka.getPrijava().getKandidat().getId().equals(kandidatId));

        boolean mozeSeIzabrati = Boolean.TRUE.equals(termin.getDostupan()) && !jeProsao(termin) && !vecIzabranOdKandidata;

        return new IspitniTerminDto(
                termin.getId(),
                termin.getTipIspita().getId(),
                termin.getTipIspita().getNaziv(),
                termin.getDatumOdrzavanja(),
                termin.getVremePocetka(),
                termin.getNapomena(),
                termin.getMestoOdrzavanja().getId(),
                termin.getMestoOdrzavanja().getNaziv(),
                termin.getMestoOdrzavanja().getAdresa(),
                izracunajTrenutnuCenu(termin),
                termin.getDostupan(),
                aktivne.size(),
                mozeSeIzabrati
        );
    }
}

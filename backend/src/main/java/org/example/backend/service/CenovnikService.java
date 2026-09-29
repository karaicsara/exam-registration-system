package org.example.backend.service;

import org.example.backend.dto.CenovnikDto;
import org.example.backend.dto.CenovnikZahtevDto;
import org.example.backend.entity.Cenovnik;
import org.example.backend.entity.TipIspita;
import org.example.backend.entity.TipIspitaCenovnik;
import org.example.backend.repository.CenovnikRepository;
import org.example.backend.repository.TipIspitaCenovnikRepository;
import org.example.backend.repository.TipIspitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CenovnikService {

    private final CenovnikRepository cenovnikRepository;
    private final TipIspitaCenovnikRepository tipIspitaCenovnikRepository;
    private final TipIspitaRepository tipIspitaRepository;

    @Transactional(readOnly = true)
    public List<CenovnikDto> getAktivneCene() {
        return tipIspitaRepository.findAll().stream()
                .map(tip -> tipIspitaCenovnikRepository.findByTipIspitaIdAndAktivanTrue(tip.getId())
                        .map(this::toDto)
                        .orElse(null))
                .filter(dto -> dto != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CenovnikDto> getIstorijaZaTip(Long tipIspitaId) {
        return tipIspitaCenovnikRepository.findByTipIspitaIdOrderByCenovnikVaziOdDesc(tipIspitaId).stream()
                .map(this::toDto)
                .toList();
    }

    // Nova cena se ne upisuje preko postojeceg Cenovnik reda, vec se pravi nov Cenovnik + nova
    // aktivna veza u TipIspitaCenovnik, dok se prethodna veza za taj tip ispita deaktivira - ovim
    // se cuva istorija cena i postuje asocijativna priroda TipIspitaCenovnik iz konceptualnog modela.
    @Transactional
    public CenovnikDto kreirajNovuCenu(CenovnikZahtevDto zahtev) {
        if (zahtev.tipIspitaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tip ispita je obavezan");
        }
        if (zahtev.iznos() == null || zahtev.iznos() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cena mora biti pozitivan broj");
        }
        if (zahtev.vaziOd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datum početka važenja je obavezan");
        }
        if (zahtev.vaziDo() != null && zahtev.vaziDo().isBefore(zahtev.vaziOd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datum prestanka važenja ne sme biti pre datuma početka");
        }
        if (zahtev.naziv() == null || zahtev.naziv().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv cenovnika je obavezan");
        }

        TipIspita tipIspita = tipIspitaRepository.findById(zahtev.tipIspitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izabrani tip ispita ne postoji"));

        tipIspitaCenovnikRepository.findByTipIspitaIdAndAktivanTrue(tipIspita.getId())
                .ifPresent(staraVeza -> {
                    staraVeza.setAktivan(false);
                    tipIspitaCenovnikRepository.save(staraVeza);
                });

        Cenovnik cenovnik = new Cenovnik();
        cenovnik.setNaziv(zahtev.naziv().trim());
        cenovnik.setIznos(zahtev.iznos());
        cenovnik.setVaziOd(zahtev.vaziOd());
        cenovnik.setVaziDo(zahtev.vaziDo());
        cenovnik.setAktivan(true);
        cenovnik = cenovnikRepository.save(cenovnik);

        TipIspitaCenovnik novaVeza = new TipIspitaCenovnik();
        novaVeza.setTipIspita(tipIspita);
        novaVeza.setCenovnik(cenovnik);
        novaVeza.setAktivan(true);
        novaVeza = tipIspitaCenovnikRepository.save(novaVeza);

        return toDto(novaVeza);
    }

    private CenovnikDto toDto(TipIspitaCenovnik veza) {
        Cenovnik cenovnik = veza.getCenovnik();
        return new CenovnikDto(
                cenovnik.getId(),
                veza.getTipIspita().getId(),
                veza.getTipIspita().getNaziv(),
                cenovnik.getNaziv(),
                cenovnik.getIznos(),
                cenovnik.getVaziOd(),
                cenovnik.getVaziDo(),
                veza.getAktivan()
        );
    }
}

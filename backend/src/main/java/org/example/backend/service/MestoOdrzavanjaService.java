package org.example.backend.service;

import org.example.backend.dto.MestoOdrzavanjaDto;
import org.example.backend.dto.MestoOdrzavanjaZahtevDto;
import org.example.backend.entity.MestoOdrzavanja;
import org.example.backend.repository.MestoOdrzavanjaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MestoOdrzavanjaService {

    private final MestoOdrzavanjaRepository mestoOdrzavanjaRepository;

    @Transactional(readOnly = true)
    public List<MestoOdrzavanjaDto> getSvaMesta() {
        return mestoOdrzavanjaRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public MestoOdrzavanjaDto kreirajMesto(MestoOdrzavanjaZahtevDto zahtev) {
        validiraj(zahtev);
        MestoOdrzavanja mesto = new MestoOdrzavanja();
        mesto.setNaziv(zahtev.naziv().trim());
        mesto.setAdresa(zahtev.adresa().trim());
        return toDto(mestoOdrzavanjaRepository.save(mesto));
    }

    @Transactional
    public MestoOdrzavanjaDto izmeniMesto(Long id, MestoOdrzavanjaZahtevDto zahtev) {
        validiraj(zahtev);
        MestoOdrzavanja mesto = mestoOdrzavanjaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesto održavanja nije pronađeno"));

        mesto.setNaziv(zahtev.naziv().trim());
        mesto.setAdresa(zahtev.adresa().trim());
        return toDto(mestoOdrzavanjaRepository.save(mesto));
    }

    @Transactional
    public void obrisiMesto(Long id) {
        MestoOdrzavanja mesto = mestoOdrzavanjaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesto održavanja nije pronađeno"));

        if (!mesto.getIspitniTermini().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mesto ima zakazane termine i ne može se obrisati");
        }
        mestoOdrzavanjaRepository.delete(mesto);
    }

    private void validiraj(MestoOdrzavanjaZahtevDto zahtev) {
        if (zahtev.naziv() == null || zahtev.naziv().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Naziv mesta je obavezan");
        }
        if (zahtev.adresa() == null || zahtev.adresa().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresa je obavezna");
        }
    }

    private MestoOdrzavanjaDto toDto(MestoOdrzavanja mesto) {
        return new MestoOdrzavanjaDto(mesto.getId(), mesto.getNaziv(), mesto.getAdresa());
    }
}

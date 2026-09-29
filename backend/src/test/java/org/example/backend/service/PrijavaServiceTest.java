package org.example.backend.service;

import org.example.backend.entity.Kandidat;
import org.example.backend.entity.Prijava;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.IspitniTerminRepository;
import org.example.backend.repository.KandidatRepository;
import org.example.backend.repository.PrijavaRepository;
import org.example.backend.repository.StavkaPrijaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PrijavaServiceTest {

    @Mock
    private PrijavaRepository prijavaRepository;
    @Mock
    private KandidatRepository kandidatRepository;
    @Mock
    private IspitniTerminRepository ispitniTerminRepository;
    @Mock
    private StavkaPrijaveRepository stavkaPrijaveRepository;
    @Mock
    private IspitniTerminService ispitniTerminService;

    private PrijavaService prijavaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        prijavaService = new PrijavaService(
                prijavaRepository, kandidatRepository, ispitniTerminRepository,
                stavkaPrijaveRepository, ispitniTerminService, mock(AsyncMailSender.class));
    }

    @Test
    void kreirajPrijavu_bacaGreskuKadaNijeIzabranNijedanTermin() {
        assertThatThrownBy(() -> prijavaService.kreirajPrijavu(1L, Collections.emptyList()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bar jedan termin");
    }

    @Test
    void kreirajPrijavu_bacaGreskuKadaKandidatNePostoji() {
        org.mockito.Mockito.when(kandidatRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> prijavaService.kreirajPrijavu(1L, List.of(10L)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Kandidat nije pronađen");
    }

    @Test
    void mozeSeOtkazati_tacnoSamoZaStatusKreirana() {
        Prijava kreirana = new Prijava();
        kreirana.setKandidat(new Kandidat());
        kreirana.setStatus(PrijavaService.STATUS_KREIRANA);
        assertThat(prijavaService.mozeSeOtkazati(kreirana)).isTrue();

        Prijava prijavljena = new Prijava();
        prijavljena.setStatus(PrijavaService.STATUS_PRIJAVLJENA);
        assertThat(prijavaService.mozeSeOtkazati(prijavljena)).isFalse();

        Prijava otkazana = new Prijava();
        otkazana.setStatus(PrijavaService.STATUS_OTKAZANA);
        assertThat(prijavaService.mozeSeOtkazati(otkazana)).isFalse();
    }

    @Test
    void mozeSeDostaviUplata_samoDokJeStatusKreirana() {
        Prijava kreirana = new Prijava();
        kreirana.setStatus(PrijavaService.STATUS_KREIRANA);
        assertThat(prijavaService.mozeSeDostaviUplata(kreirana)).isTrue();

        Prijava prijavljena = new Prijava();
        prijavljena.setStatus(PrijavaService.STATUS_PRIJAVLJENA);
        assertThat(prijavaService.mozeSeDostaviUplata(prijavljena)).isFalse();
    }
}

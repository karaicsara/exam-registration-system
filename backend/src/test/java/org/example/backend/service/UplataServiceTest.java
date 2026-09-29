package org.example.backend.service;

import org.example.backend.entity.Administrator;
import org.example.backend.entity.Kandidat;
import org.example.backend.entity.Prijava;
import org.example.backend.entity.Uplata;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.AdministratorRepository;
import org.example.backend.repository.PrijavaRepository;
import org.example.backend.repository.UplataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UplataServiceTest {

    @Mock
    private UplataRepository uplataRepository;
    @Mock
    private PrijavaRepository prijavaRepository;
    @Mock
    private AdministratorRepository administratorRepository;
    @Mock
    private PrijavaService prijavaService;

    private UplataService uplataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uplataService = new UplataService(uplataRepository, prijavaRepository, administratorRepository, prijavaService, mock(AsyncMailSender.class));
    }

    @Test
    void odobriUplatu_bacaGreskuAkoUplataNijeNaCekanju() {
        Administrator administrator = new Administrator();
        administrator.setId(1L);
        administrator.setIme("Ana");
        administrator.setPrezime("Anić");
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        Kandidat kandidat = new Kandidat();
        kandidat.setEmail("kandidat@fon.bg.ac.rs");
        Prijava prijava = new Prijava();
        prijava.setKandidat(kandidat);

        Uplata uplata = new Uplata();
        uplata.setId(5L);
        uplata.setStatus(UplataService.STATUS_ODOBRENA);
        uplata.setPrijava(prijava);
        when(uplataRepository.findById(5L)).thenReturn(Optional.of(uplata));

        assertThatThrownBy(() -> uplataService.odobriUplatu(1L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("već obrađena");
    }

    @Test
    void odbijUplatu_bacaGreskuKadaRazlogNijeUnet() {
        Administrator administrator = new Administrator();
        administrator.setId(1L);
        when(administratorRepository.findById(1L)).thenReturn(Optional.of(administrator));

        assertThatThrownBy(() -> uplataService.odbijUplatu(1L, 5L, "  "))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Razlog odbijanja");
    }

    @Test
    void posaljiDokazUplate_bacaGreskuKadaPrijavaNijePronadjena() {
        when(prijavaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uplataService.posaljiDokazUplate(1L, 99L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Prijava nije pronađena");
    }
}

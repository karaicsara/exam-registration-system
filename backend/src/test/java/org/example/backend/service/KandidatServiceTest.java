package org.example.backend.service;

import org.example.backend.dto.RegistracijaKandidataDto;
import org.example.backend.dto.VerifikacijaKodaDto;
import org.example.backend.mail.AsyncMailSender;
import org.example.backend.repository.KandidatRepository;
import org.example.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KandidatServiceTest {

    @Mock
    private KandidatRepository kandidatRepository;

    private KandidatService kandidatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kandidatService = new KandidatService(kandidatRepository, mock(JwtUtil.class), mock(AsyncMailSender.class));
    }

    @Test
    void zapocniRegistraciju_bacaGreskuZaNevalidanEmail() {
        RegistracijaKandidataDto dto = new RegistracijaKandidataDto("Marko", "Marković", "0601234567", "marko", "lozinka1", "nije-email");

        assertThatThrownBy(() -> kandidatService.zapocniRegistraciju(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email nije validan");
    }

    @Test
    void zapocniRegistraciju_bacaGreskuZaKratkuLozinku() {
        RegistracijaKandidataDto dto = new RegistracijaKandidataDto("Marko", "Marković", "0601234567", "marko", "123", "marko@primer.com");

        assertThatThrownBy(() -> kandidatService.zapocniRegistraciju(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Lozinka");
    }

    @Test
    void zapocniRegistraciju_bacaGreskuKadaJeEmailVecZauzet() {
        RegistracijaKandidataDto dto = new RegistracijaKandidataDto("Marko", "Marković", "0601234567", "marko", "lozinka1", "marko@primer.com");
        when(kandidatRepository.existsByEmail("marko@primer.com")).thenReturn(true);

        assertThatThrownBy(() -> kandidatService.zapocniRegistraciju(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("već registrovan");
    }

    @Test
    void verifikujKod_bacaGreskuKadaRegistracijaNijeZapoceta() {
        VerifikacijaKodaDto dto = new VerifikacijaKodaDto("nepostojeci@primer.com", "123456");

        assertThatThrownBy(() -> kandidatService.verifikujKodIZavrsiRegistraciju(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nije započeta");
    }
}

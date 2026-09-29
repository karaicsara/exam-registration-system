package org.example.backend.service;

import org.example.backend.dto.TipIspitaDto;
import org.example.backend.repository.TipIspitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipIspitaService {

    private final TipIspitaRepository tipIspitaRepository;

    // Fakultet organizuje tacno dve oblasti probnog prijemnog ispita (matematika i test opste
    // informisanosti) - to je fiksna cinjenica domena, zato ne postoji CRUD za ovaj entitet.
    @Transactional(readOnly = true)
    public List<TipIspitaDto> getSviTipovi() {
        return tipIspitaRepository.findAll().stream()
                .map(tip -> new TipIspitaDto(tip.getId(), tip.getNaziv()))
                .toList();
    }
}

package org.example.backend.controller;

import org.example.backend.dto.TipIspitaDto;
import org.example.backend.service.TipIspitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipovi-ispita")
@RequiredArgsConstructor
public class TipIspitaController {

    private final TipIspitaService tipIspitaService;

    @GetMapping
    public List<TipIspitaDto> getSviTipovi() {
        return tipIspitaService.getSviTipovi();
    }
}

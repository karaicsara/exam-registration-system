package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class IspitniTermin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate datumOdrzavanja;
    private String vremePocetka;
    private String napomena;
    private Boolean dostupan;

    @ManyToOne
    @JoinColumn(name = "mesto_odrzavanja_id")
    private MestoOdrzavanja mestoOdrzavanja;

    @ManyToOne
    @JoinColumn(name = "tip_ispita_id")
    private TipIspita tipIspita;

    @OneToMany(mappedBy = "ispitniTermin")
    private List<StavkaPrijave> stavkePrijave = new ArrayList<>();

    public IspitniTermin() {
    }
}

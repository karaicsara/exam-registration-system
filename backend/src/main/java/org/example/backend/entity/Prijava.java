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
public class Prijava {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate datumKreiranja;
    private Double popust;
    private Double ukupanIznos;
    private String status;
    private String napomena;

    @ManyToOne
    @JoinColumn(name = "kandidat_id")
    private Kandidat kandidat;

    @ManyToOne
    @JoinColumn(name = "administrator_id")
    private Administrator administrator;

    @OneToMany(mappedBy = "prijava")
    private List<StavkaPrijave> stavkePrijave = new ArrayList<>();

    @OneToMany(mappedBy = "prijava")
    private List<Uplata> uplate = new ArrayList<>();

    public Prijava() {
    }
}

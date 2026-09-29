package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Cenovnik {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String naziv;
    private Double iznos;
    private LocalDate vaziOd;
    private LocalDate vaziDo;
    private Boolean aktivan;

    @OneToMany(mappedBy = "cenovnik")
    private List<TipIspitaCenovnik> tipIspitaCenovnici = new ArrayList<>();

    public Cenovnik() {
    }
}

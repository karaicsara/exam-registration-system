package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class StavkaPrijave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double cenaNaDanPrijave;

    @ManyToOne
    @JoinColumn(name = "ispitni_termin_id")
    private IspitniTermin ispitniTermin;

    @ManyToOne
    @JoinColumn(name = "prijava_id")
    private Prijava prijava;

    public StavkaPrijave() {
    }
}

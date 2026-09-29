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
public class TipIspitaCenovnik {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean aktivan;

    @ManyToOne
    @JoinColumn(name = "tip_ispita_id")
    private TipIspita tipIspita;

    @ManyToOne
    @JoinColumn(name = "cenovnik_id")
    private Cenovnik cenovnik;

    public TipIspitaCenovnik() {
    }
}

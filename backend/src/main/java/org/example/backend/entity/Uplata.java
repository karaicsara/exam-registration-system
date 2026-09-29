package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Uplata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String putanjaDokumenta;
    private LocalDate datumSlanja;
    private String status;
    private String napomena;

    @ManyToOne
    @JoinColumn(name = "prijava_id")
    private Prijava prijava;

    public Uplata() {
    }
}

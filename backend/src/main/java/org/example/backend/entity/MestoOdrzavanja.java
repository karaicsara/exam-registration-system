package org.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class MestoOdrzavanja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String naziv;
    private String adresa;

    @OneToMany(mappedBy = "mestoOdrzavanja")
    private List<IspitniTermin> ispitniTermini = new ArrayList<>();

    public MestoOdrzavanja() {
    }
}

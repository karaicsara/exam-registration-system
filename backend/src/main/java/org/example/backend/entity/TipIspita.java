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
public class TipIspita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String naziv;

    @OneToMany(mappedBy = "tipIspita")
    private List<IspitniTermin> ispitniTermini = new ArrayList<>();

    @OneToMany(mappedBy = "tipIspita")
    private List<TipIspitaCenovnik> tipIspitaCenovnici = new ArrayList<>();

    public TipIspita() {
    }
}

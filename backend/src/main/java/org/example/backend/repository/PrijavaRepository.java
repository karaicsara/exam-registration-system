package org.example.backend.repository;

import org.example.backend.entity.Prijava;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrijavaRepository extends JpaRepository<Prijava, Long> {
    List<Prijava> findByKandidatIdOrderByDatumKreiranjaDesc(Long kandidatId);
}

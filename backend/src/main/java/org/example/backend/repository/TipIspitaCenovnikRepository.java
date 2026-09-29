package org.example.backend.repository;

import org.example.backend.entity.TipIspitaCenovnik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipIspitaCenovnikRepository extends JpaRepository<TipIspitaCenovnik, Long> {
    Optional<TipIspitaCenovnik> findByTipIspitaIdAndAktivanTrue(Long tipIspitaId);

    List<TipIspitaCenovnik> findByTipIspitaIdOrderByCenovnikVaziOdDesc(Long tipIspitaId);
}

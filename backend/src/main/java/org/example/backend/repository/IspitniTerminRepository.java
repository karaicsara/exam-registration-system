package org.example.backend.repository;

import org.example.backend.entity.IspitniTermin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IspitniTerminRepository extends JpaRepository<IspitniTermin, Long> {
    List<IspitniTermin> findByTipIspitaId(Long tipIspitaId);

    List<IspitniTermin> findAllByIdIn(List<Long> ids);

    List<IspitniTermin> findAllByOrderByDatumOdrzavanjaAscVremePocetkaAsc();
}

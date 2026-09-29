package org.example.backend.repository;

import org.example.backend.entity.StavkaPrijave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StavkaPrijaveRepository extends JpaRepository<StavkaPrijave, Long> {
    List<StavkaPrijave> findByPrijavaId(Long prijavaId);

    List<StavkaPrijave> findByIspitniTerminId(Long ispitniTerminId);
}

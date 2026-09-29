package org.example.backend.repository;

import org.example.backend.entity.Uplata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UplataRepository extends JpaRepository<Uplata, Long> {
    List<Uplata> findByPrijavaIdOrderByDatumSlanjaDesc(Long prijavaId);
}

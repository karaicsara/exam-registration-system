package org.example.backend.repository;

import org.example.backend.entity.Cenovnik;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenovnikRepository extends JpaRepository<Cenovnik, Long> {
}

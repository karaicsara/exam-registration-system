package org.example.backend.repository;

import org.example.backend.entity.Kandidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KandidatRepository extends JpaRepository<Kandidat, Long> {
    Optional<Kandidat> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

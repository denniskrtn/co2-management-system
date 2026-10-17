package de.co2management.backend.emission;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionSourceRepository extends JpaRepository<EmissionSource, Long> {
}
package de.co2management.backend.emission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {

    Optional<EmissionFactor> findByEmissionSourceIdAndYear(Long emissionSourceId, int year);
}
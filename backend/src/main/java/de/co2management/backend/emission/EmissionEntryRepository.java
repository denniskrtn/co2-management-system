package de.co2management.backend.emission;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionEntryRepository extends JpaRepository<EmissionEntry, Long> {
}
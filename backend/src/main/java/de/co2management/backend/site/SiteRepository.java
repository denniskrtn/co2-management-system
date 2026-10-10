package de.co2management.backend.site;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository <Site, Long> {

    Optional<Site> findByCode(String code);

    boolean existsByCode(String code);
}

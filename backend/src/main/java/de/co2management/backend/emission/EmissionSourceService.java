package de.co2management.backend.emission;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmissionSourceService {

    private final EmissionSourceRepository sourceRepository;

    public EmissionSourceService(EmissionSourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @PreAuthorize("hasRole('SUSTAINABILITY_OFFICER')")
    @Transactional(readOnly = true)
    public List<EmissionSourceInfo> findAllSources() {
        return sourceRepository.findAll(Sort.by("id")).stream()
                .map(EmissionSourceInfo::from)
                .toList();
    }
}
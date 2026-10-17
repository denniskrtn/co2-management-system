package de.co2management.backend.emission;

import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
@PreAuthorize("hasRole('SUSTAINABILITY_OFFICER')")
public class EmissionEntryService {

    private final EmissionEntryRepository entryRepository;
    private final SiteRepository siteRepository;
    private final EmissionSourceRepository sourceRepository;
    private final EmissionFactorRepository factorRepository;
    private final EmissionCalculator calculator;
    private final Clock clock;

    public EmissionEntryService(EmissionEntryRepository entryRepository,
                                SiteRepository siteRepository,
                                EmissionSourceRepository sourceRepository,
                                EmissionFactorRepository factorRepository,
                                EmissionCalculator calculator,
                                Clock clock) {
        this.entryRepository = entryRepository;
        this.siteRepository = siteRepository;
        this.sourceRepository = sourceRepository;
        this.factorRepository = factorRepository;
        this.calculator = calculator;
        this.clock = clock;
    }

    @Transactional
    public EntryInfo create(CreateEntryRequest request) {
        if (request.entryDate().isAfter(LocalDate.now(clock))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Das Datum darf nicht in der Zukunft liegen");
        }
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Standort nicht gefunden"));
        EmissionSource source = sourceRepository.findById(request.emissionSourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Emissionsquelle nicht gefunden"));

        int year = request.entryDate().getYear();
        EmissionFactor factor = factorRepository.findByEmissionSourceIdAndYear(request.emissionSourceId(), year)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Für " + source.getName() + " ist für das Jahr " + year + " kein Emissionsfaktor hinterlegt"));

        BigDecimal co2eKg = calculator.calculate(request.quantity(), factor.getFactorValue());
        EmissionEntry entry = entryRepository.save(
                new EmissionEntry(site, factor, request.quantity(), request.entryDate(), co2eKg));
        return EntryInfo.from(entry);
    }
}
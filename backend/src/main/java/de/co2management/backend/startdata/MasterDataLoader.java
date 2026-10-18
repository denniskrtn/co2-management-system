package de.co2management.backend.startdata;

import de.co2management.backend.emission.EmissionFactor;
import de.co2management.backend.emission.EmissionFactorRepository;
import de.co2management.backend.emission.EmissionSource;
import de.co2management.backend.emission.EmissionSourceRepository;
import de.co2management.backend.emission.Scope;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

@Order(1)
@Component
public class MasterDataLoader implements CommandLineRunner {

    private final EmissionSourceRepository sourceRepository;
    private final EmissionFactorRepository factorRepository;

    public MasterDataLoader(EmissionSourceRepository sourceRepository,
                            EmissionFactorRepository factorRepository) {
        this.sourceRepository = sourceRepository;
        this.factorRepository = factorRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (sourceRepository.count() > 0) {
            return;
        }
        createSource("Strom", "kWh", Scope.SCOPE_2, "0.400", "0.380");
        createSource("Diesel", "Liter", Scope.SCOPE_1, "2.500", "2.500");
        createSource("Benzin", "Liter", Scope.SCOPE_1, "2.300", "2.300");
        createSource("Bahnreise", "km", Scope.SCOPE_3, "0.050", "0.040");
        createSource("Flugreise", "km", Scope.SCOPE_3, "0.200", "0.200");
    }

    private void createSource(String name, String unit, Scope scope,
                              String factor2025, String factor2026) {
        EmissionSource source = sourceRepository.save(new EmissionSource(name, unit, scope));
        factorRepository.save(new EmissionFactor(source, 2025, new BigDecimal(factor2025)));
        factorRepository.save(new EmissionFactor(source, 2026, new BigDecimal(factor2026)));
    }
}
package de.co2management.backend.startdata;

import de.co2management.backend.emission.EmissionCalculator;
import de.co2management.backend.emission.EmissionEntry;
import de.co2management.backend.emission.EmissionEntryRepository;
import de.co2management.backend.emission.EmissionFactor;
import de.co2management.backend.emission.EmissionFactorRepository;
import de.co2management.backend.emission.EmissionSource;
import de.co2management.backend.emission.EmissionSourceRepository;
import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Order(3)
public class DemoEntryLoader implements CommandLineRunner {

    private static final LocalDate FIRST_MONTH = LocalDate.of(2025, 10, 1);

    private static final String[] NORD_STROM = {"41800", "44500", "46200", "47900", "45300", "43100",
            "40200", "38600", "37400", "36900", "35800", "39100"};
    private static final String[] NORD_DIESEL = {"1850", "1920", "1640", "1780", "1810", "1950",
            "2010", "1980", "2050", "1720", "1430", "1890"};
    private static final String[] NORD_BAHN = {"2400", "3100", "1200", "2800", "3300", "2950",
            "2600", "3400", "2200", "1500", "900", "2750"};
    private static final String[] SUED_STROM = {"21500", "23200", "24800", "25600", "24100", "22700",
            "20900", "19800", "19100", "18700", "18200", "20400"};
    private static final String[] SUED_BENZIN = {"620", "650", "540", "600", "610", "680",
            "700", "690", "720", "580", "460", "640"};
    private static final String[] SUED_BAHN = {"1200", "1500", "600", "1400", "1650", "1450",
            "1300", "1700", "1100", "800", "450", "1350"};

    private final SiteRepository siteRepository;
    private final EmissionSourceRepository sourceRepository;
    private final EmissionFactorRepository factorRepository;
    private final EmissionEntryRepository entryRepository;
    private final EmissionCalculator calculator;

    public DemoEntryLoader(SiteRepository siteRepository,
                           EmissionSourceRepository sourceRepository,
                           EmissionFactorRepository factorRepository,
                           EmissionEntryRepository entryRepository,
                           EmissionCalculator calculator) {
        this.siteRepository = siteRepository;
        this.sourceRepository = sourceRepository;
        this.factorRepository = factorRepository;
        this.entryRepository = entryRepository;
        this.calculator = calculator;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (entryRepository.count() > 0) {
            return;
        }
        Site nord = siteRepository.findByCode("NORD").orElse(null);
        Site sued = siteRepository.findByCode("SUED").orElse(null);
        Site west = siteRepository.findByCode("WEST").orElse(null);
        if (nord == null || sued == null || west == null) {
            return;
        }
        Map<String, EmissionSource> sources = sourceRepository.findAll().stream()
                .collect(Collectors.toMap(EmissionSource::getName, Function.identity()));

        for (int i = 0; i < 12; i++) {
            LocalDate month = FIRST_MONTH.plusMonths(i);
            add(nord, sources.get("Strom"), month.withDayOfMonth(28), NORD_STROM[i]);
            add(nord, sources.get("Diesel"), month.withDayOfMonth(15), NORD_DIESEL[i]);
            add(nord, sources.get("Bahnreise"), month.withDayOfMonth(10), NORD_BAHN[i]);
            add(sued, sources.get("Strom"), month.withDayOfMonth(28), SUED_STROM[i]);
            add(sued, sources.get("Benzin"), month.withDayOfMonth(15), SUED_BENZIN[i]);
            add(sued, sources.get("Bahnreise"), month.withDayOfMonth(10), SUED_BAHN[i]);
        }

        add(nord, sources.get("Flugreise"), LocalDate.of(2025, 11, 12), "1200");
        add(nord, sources.get("Flugreise"), LocalDate.of(2026, 2, 18), "1200");
        add(nord, sources.get("Flugreise"), LocalDate.of(2026, 5, 6), "1200");
        add(nord, sources.get("Flugreise"), LocalDate.of(2026, 9, 22), "1200");
        add(sued, sources.get("Flugreise"), LocalDate.of(2026, 3, 11), "950");
        add(sued, sources.get("Flugreise"), LocalDate.of(2026, 6, 24), "950");

        add(west, sources.get("Strom"), LocalDate.of(2026, 7, 28), "8200");
        add(west, sources.get("Strom"), LocalDate.of(2026, 8, 28), "12500");
        add(west, sources.get("Strom"), LocalDate.of(2026, 9, 28), "14800");
        add(west, sources.get("Diesel"), LocalDate.of(2026, 7, 15), "320");
        add(west, sources.get("Diesel"), LocalDate.of(2026, 8, 15), "480");
        add(west, sources.get("Diesel"), LocalDate.of(2026, 9, 15), "510");
    }

    private void add(Site site, EmissionSource source, LocalDate date, String quantity) {
        EmissionFactor factor = factorRepository
                .findByEmissionSourceIdAndYear(source.getId(), date.getYear())
                .orElseThrow();
        BigDecimal amount = new BigDecimal(quantity);
        entryRepository.save(new EmissionEntry(site, factor, amount, date,
                calculator.calculate(amount, factor.getFactorValue())));
    }
}
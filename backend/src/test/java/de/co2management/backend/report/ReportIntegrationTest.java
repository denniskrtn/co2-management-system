package de.co2management.backend.report;

import de.co2management.backend.TestcontainersConfiguration;
import de.co2management.backend.emission.EmissionEntry;
import de.co2management.backend.emission.EmissionEntryRepository;
import de.co2management.backend.emission.EmissionFactor;
import de.co2management.backend.emission.EmissionFactorRepository;
import de.co2management.backend.emission.EmissionSourceRepository;
import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import de.co2management.backend.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private EmissionSourceRepository sourceRepository;

    @Autowired
    private EmissionFactorRepository factorRepository;

    @Autowired
    private EmissionEntryRepository entryRepository;

    private Site nord;
    private Site sued;
    private Site west;
    private EmissionFactor factor;

    @BeforeEach
    void setUp() {
        nord = siteRepository.save(new Site("T06N", "Test Nord"));
        sued = siteRepository.save(new Site("T06S", "Test Süd"));
        west = siteRepository.save(new Site("T06W", "Test West"));
        Long stromId = sourceRepository.findAll().stream()
                .filter(source -> source.getName().equals("Strom"))
                .findFirst().orElseThrow().getId();
        factor = factorRepository.findByEmissionSourceIdAndYear(stromId, 2026).orElseThrow();
    }

    @AfterEach
    void cleanUp() {
        entryRepository.deleteAll(entryRepository.findAll().stream()
                .filter(entry -> entry.getSite().getCode().startsWith("T06"))
                .toList());
        for (String code : List.of("T06N", "T06S", "T06W")) {
            siteRepository.findByCode(code).ifPresent(siteRepository::delete);
        }
    }

    private void entry(Site site, LocalDate date, String co2eKg) {
        entryRepository.save(new EmissionEntry(site, factor, BigDecimal.ONE, date, new BigDecimal(co2eKg)));
    }

    private RequestPostProcessor as(Role role, Site site) {
        return jwt().jwt(builder -> {
            builder.claim("role", role.name());
            if (site != null) {
                builder.claim("siteId", site.getId());
            }
        }).authorities(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Test
    @DisplayName("T-06-01: Nachhaltigkeitsbeauftragte erhält eine Summe je Standort")
    void officerSeesAllSites() throws Exception {
        entry(nord, LocalDate.of(2026, 1, 15), "380");
        entry(nord, LocalDate.of(2026, 3, 15), "380");
        entry(sued, LocalDate.of(2026, 2, 28), "40");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-01-01").param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sites[*].siteName").value(hasItems("Test Nord", "Test Süd", "Test West")));

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-01-01").param("to", "2026-12-31").param("siteId", nord.getId().toString()))
                .andExpect(jsonPath("$.sites[0].totalKg").value(760.0));
    }

    @Test
    @DisplayName("T-06-02: Standort ohne Einträge erscheint mit 0")
    void siteWithoutEntriesShowsZero() throws Exception {
        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-01-01").param("to", "2026-12-31").param("siteId", west.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sites[0].siteName").value("Test West"))
                .andExpect(jsonPath("$.sites[0].totalKg").value(0.0));
    }

    @Test
    @DisplayName("T-06-03: Summe aus ungerundeten Einzelwerten")
    void sumIsNotRounded() throws Exception {
        entry(nord, LocalDate.of(2026, 3, 1), "0.004");
        entry(nord, LocalDate.of(2026, 3, 2), "0.004");
        entry(nord, LocalDate.of(2026, 3, 3), "0.004");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-01-01").param("to", "2026-12-31").param("siteId", nord.getId().toString()))
                .andExpect(jsonPath("$.sites[0].totalKg").value(0.012));
    }

    @Test
    @DisplayName("T-06-05: Ohne Zeitraum zählt nur das laufende Kalenderjahr")
    void withoutPeriodOnlyCurrentYearCounts() throws Exception {
        int year = LocalDate.now().getYear();
        entry(nord, LocalDate.of(year, 1, 1), "380");
        entry(nord, LocalDate.of(year - 1, 12, 31), "250");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("siteId", nord.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value(year + "-01-01"))
                .andExpect(jsonPath("$.to").value(year + "-12-31"))
                .andExpect(jsonPath("$.sites[0].totalKg").value(380.0));
    }

    @Test
    @DisplayName("T-07-01 bis T-07-04: Start- und Endtag zählen mit, die Tage davor und danach nicht")
    void periodBoundariesAreInclusive() throws Exception {
        entry(nord, LocalDate.of(2026, 2, 28), "1");
        entry(nord, LocalDate.of(2026, 3, 1), "10");
        entry(nord, LocalDate.of(2026, 3, 31), "100");
        entry(nord, LocalDate.of(2026, 4, 1), "1000");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-03-01").param("to", "2026-03-31").param("siteId", nord.getId().toString()))
                .andExpect(jsonPath("$.sites[0].totalKg").value(110.0));
    }

    @Test
    @DisplayName("T-07-05: Startdatum nach Enddatum wird abgewiesen")
    void startAfterEndIsRejected() throws Exception {
        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-04-01").param("to", "2026-03-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-07-06: Fehlendes Enddatum wird abgewiesen")
    void missingEndIsRejected() throws Exception {
        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-07-07: Zeitraum ohne Einträge ergibt 0")
    void periodWithoutEntriesShowsZero() throws Exception {
        entry(nord, LocalDate.of(2026, 3, 15), "380");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.SUSTAINABILITY_OFFICER, null))
                        .param("from", "2020-01-01").param("to", "2020-12-31").param("siteId", nord.getId().toString()))
                .andExpect(jsonPath("$.sites[0].totalKg").value(0.0));
    }

    @Test
    @DisplayName("T-08-01: Benutzer in Süd erhält nur Süd")
    void userSeesOnlyOwnSite() throws Exception {
        entry(nord, LocalDate.of(2026, 3, 15), "380");
        entry(sued, LocalDate.of(2026, 3, 15), "40");

        mockMvc.perform(get("/api/reports/totals").with(as(Role.USER, sued))
                        .param("from", "2026-01-01").param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sites.length()").value(1))
                .andExpect(jsonPath("$.sites[0].siteName").value("Test Süd"))
                .andExpect(jsonPath("$.sites[0].totalKg").value(40.0));
    }

    @Test
    @DisplayName("T-08-03: Benutzer mit der Kennung eines fremden Standorts wird abgewiesen")
    void userCannotRequestForeignSite() throws Exception {
        mockMvc.perform(get("/api/reports/totals").with(as(Role.USER, sued))
                        .param("siteId", nord.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T-08-04: Administration erhält keine Summen")
    void adminCannotSeeTotals() throws Exception {
        mockMvc.perform(get("/api/reports/totals").with(as(Role.ADMIN, null)))
                .andExpect(status().isForbidden());
    }
}
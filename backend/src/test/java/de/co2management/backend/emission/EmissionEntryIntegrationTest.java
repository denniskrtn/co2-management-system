package de.co2management.backend.emission;

import de.co2management.backend.TestcontainersConfiguration;
import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class EmissionEntryIntegrationTest {

    private static final SimpleGrantedAuthority OFFICER = new SimpleGrantedAuthority("ROLE_SUSTAINABILITY_OFFICER");
    private static final SimpleGrantedAuthority MANAGER = new SimpleGrantedAuthority("ROLE_MANAGER");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

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

    private Site site;
    private EmissionSource strom;

    @BeforeEach
    void setUp() {
        site = siteRepository.save(new Site("T05A", "Werk Test"));
        strom = sourceRepository.findAll().stream()
                .filter(source -> source.getName().equals("Strom"))
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void cleanUp() {
        entryRepository.deleteAll(entryRepository.findAll().stream()
                .filter(entry -> entry.getSite().getCode().startsWith("T05"))
                .toList());
        siteRepository.findByCode("T05A").ifPresent(siteRepository::delete);
    }

    private String entryJson(Long siteId, Long sourceId) {
        return """
                {"siteId": %d, "emissionSourceId": %d, "quantity": 1000, "entryDate": "2026-03-15"}
                """.formatted(siteId, sourceId);
    }

    @Test
    @DisplayName("T-05-12: Gültiger Eintrag wird mit Faktorwert gespeichert, Antwort enthält Ergebnis")
    void validEntryIsSaved() throws Exception {
        mockMvc.perform(post("/api/entries").with(jwt().authorities(OFFICER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson(site.getId(), strom.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.co2eKg").value(380.0))
                .andExpect(jsonPath("$.factorValue").value(0.38));

        EmissionEntry saved = entryRepository.findAll().stream()
                .filter(entry -> entry.getSite().getId().equals(site.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(saved.getFactorValue()).isEqualByComparingTo("0.380");
        assertThat(saved.getCo2eKg()).isEqualByComparingTo("380");
    }

    @Test
    @DisplayName("T-05-13: Nicht existierender Standort wird abgewiesen")
    void unknownSiteIsRejected() throws Exception {
        mockMvc.perform(post("/api/entries").with(jwt().authorities(OFFICER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson(999999L, strom.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-05-14: Nicht existierende Emissionsquelle wird abgewiesen")
    void unknownSourceIsRejected() throws Exception {
        mockMvc.perform(post("/api/entries").with(jwt().authorities(OFFICER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson(site.getId(), 999999L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("T-05-15: Erfassung als Führungskraft wird verweigert")
    void managerCannotCreateEntry() throws Exception {
        mockMvc.perform(post("/api/entries").with(jwt().authorities(MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(entryJson(site.getId(), strom.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("T-03-06: Standort mit Einträgen wird nicht gelöscht")
    void siteWithEntriesIsNotDeleted() throws Exception {
        EmissionFactor factor = factorRepository.findByEmissionSourceIdAndYear(strom.getId(), 2026).orElseThrow();
        entryRepository.save(new EmissionEntry(site, factor, new BigDecimal("1000"),
                LocalDate.of(2026, 3, 15), new BigDecimal("380")));

        mockMvc.perform(delete("/api/sites/{id}", site.getId()).with(jwt().authorities(ADMIN)))
                .andExpect(status().isConflict());

        assertThat(siteRepository.existsByCode("T05A")).isTrue();
    }
}
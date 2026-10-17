package de.co2management.backend.emission;

import de.co2management.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class EmissionSourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmissionSourceRepository sourceRepository;

    @Autowired
    private EmissionFactorRepository factorRepository;

    @Test
    @DisplayName("T-02-01: Stammdaten mit fünf Quellen und Faktoren für zwei Jahre")
    void masterDataContainsFiveSourcesWithFactorsForTwoYears() throws Exception {
        mockMvc.perform(get("/api/emission-sources")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SUSTAINABILITY_OFFICER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(contains("Strom", "Diesel", "Benzin", "Bahnreise", "Flugreise")))
                .andExpect(jsonPath("$[*].unit").value(contains("kWh", "Liter", "Liter", "km", "km")))
                .andExpect(jsonPath("$[*].scope").value(contains("SCOPE_2", "SCOPE_1", "SCOPE_1", "SCOPE_3", "SCOPE_3")));

        for (EmissionSource source : sourceRepository.findAll()) {
            assertThat(factorRepository.findByEmissionSourceIdAndYear(source.getId(), 2025)).isPresent();
            assertThat(factorRepository.findByEmissionSourceIdAndYear(source.getId(), 2026)).isPresent();
        }

        BigDecimal strom2026 = factorRepository
                .findByEmissionSourceIdAndYear(findSource("Strom").getId(), 2026)
                .orElseThrow()
                .getFactorValue();
        assertThat(strom2026).isEqualByComparingTo("0.380");
    }

    @Test
    @DisplayName("T-02-02: Zweiter Faktor für gleiche Quelle und gleiches Jahr wird abgewiesen")
    void secondFactorForSameSourceAndYearIsRejected() {
        EmissionSource strom = findSource("Strom");

        assertThatThrownBy(() -> factorRepository.saveAndFlush(
                new EmissionFactor(strom, 2025, new BigDecimal("0.999"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("T-02-03: Abfrage als Führungskraft wird verweigert")
    void managerCannotQueryEmissionSources() throws Exception {
        mockMvc.perform(get("/api/emission-sources")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    private EmissionSource findSource(String name) {
        return sourceRepository.findAll().stream()
                .filter(source -> source.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
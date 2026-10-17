package de.co2management.backend.site;

import de.co2management.backend.TestcontainersConfiguration;
import de.co2management.backend.user.AppUser;
import de.co2management.backend.user.AppUserRepository;
import de.co2management.backend.user.Role;
import org.junit.jupiter.api.AfterEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class SiteIntegrationTest {

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority OFFICER = new SimpleGrantedAuthority("ROLE_SUSTAINABILITY_OFFICER");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private AppUserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.findByUsername("t03-benutzer").ifPresent(userRepository::delete);
        siteRepository.findAll().stream()
                .filter(site -> site.getCode().startsWith("T03"))
                .forEach(siteRepository::delete);
    }

    @Test
    @DisplayName("Angelegter Standort erscheint in der Liste, Kennung in Großbuchstaben")
    void createdSiteAppearsInList() throws Exception {
        mockMvc.perform(post("/api/sites").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "t03a", "name": "Werk Test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("T03A"));

        mockMvc.perform(get("/api/sites").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code").value(hasItem("T03A")));
    }

    @Test
    @DisplayName("Standort ohne Namen wird abgewiesen")
    void siteWithoutNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/sites").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "T03B", "name": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Bereits vergebene Kennung wird abgewiesen")
    void duplicateCodeIsRejected() throws Exception {
        siteRepository.save(new Site("T03C", "Erster Standort"));

        mockMvc.perform(post("/api/sites").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "t03c", "name": "Zweiter Standort"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Ändern betrifft nur den Namen, die Kennung bleibt")
    void renameChangesOnlyName() throws Exception {
        Site site = siteRepository.save(new Site("T03D", "Alter Name"));

        mockMvc.perform(put("/api/sites/{id}", site.getId()).with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "ANDERS", "name": "Neuer Name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("T03D"))
                .andExpect(jsonPath("$.name").value("Neuer Name"));
    }

    @Test
    @DisplayName("Standort ohne Zuordnungen wird gelöscht")
    void siteWithoutAssignmentsIsDeleted() throws Exception {
        Site site = siteRepository.save(new Site("T03E", "Zum Löschen"));

        mockMvc.perform(delete("/api/sites/{id}", site.getId()).with(jwt().authorities(ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(siteRepository.existsByCode("T03E")).isFalse();
    }

    @Test
    @DisplayName("T-03-06: Standort mit zugeordneten Daten wird nicht gelöscht")
    void siteWithAssignedUserIsNotDeleted() throws Exception {
        Site site = siteRepository.save(new Site("T03F", "Mit Benutzer"));
        userRepository.save(new AppUser("Test Person", "t03-benutzer", "{noop}x", Role.USER, site));

        mockMvc.perform(delete("/api/sites/{id}", site.getId()).with(jwt().authorities(ADMIN)))
                .andExpect(status().isConflict());

        assertThat(siteRepository.existsByCode("T03F")).isTrue();
    }

    @Test
    @DisplayName("T-03-07: Anlegen als Nachhaltigkeitsbeauftragte wird verweigert")
    void officerCannotCreateSite() throws Exception {
        mockMvc.perform(post("/api/sites").with(jwt().authorities(OFFICER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code": "T03G", "name": "Unerlaubt"}
                                """))
                .andExpect(status().isForbidden());
    }
}
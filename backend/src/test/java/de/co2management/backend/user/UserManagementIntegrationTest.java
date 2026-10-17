package de.co2management.backend.user;

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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class UserManagementIntegrationTest {

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    private Site site;

    @BeforeEach
    void setUp() {
        site = siteRepository.save(new Site("T04A", "Werk Test"));
    }

    @AfterEach
    void cleanUp() {
        userRepository.findAll().stream()
                .filter(user -> user.getUsername().startsWith("t04-"))
                .forEach(userRepository::delete);
        siteRepository.findByCode("T04A").ifPresent(siteRepository::delete);
    }

    @Test
    @DisplayName("Angelegtes Konto erscheint in der Liste, Anmeldename in Kleinbuchstaben")
    void createdUserAppearsInList() throws Exception {
        mockMvc.perform(post("/api/users").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test Person", "username": "T04-Neu", "password": "geheim123",
                                 "role": "MANAGER", "siteId": %d}
                                """.formatted(site.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("t04-neu"))
                .andExpect(jsonPath("$.siteName").value("Werk Test"));

        mockMvc.perform(get("/api/users").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username").value(hasItem("t04-neu")));
    }

    @Test
    @DisplayName("Bereits vergebener Anmeldename wird abgewiesen")
    void duplicateUsernameIsRejected() throws Exception {
        userRepository.save(new AppUser("Erste Person", "t04-doppelt", "{noop}x", Role.USER, site));

        mockMvc.perform(post("/api/users").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Zweite Person", "username": "t04-doppelt", "password": "geheim123",
                                 "role": "SUSTAINABILITY_OFFICER", "siteId": null}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("T-04-08: Anfrage als Benutzer wird verweigert")
    void userCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/users").with(jwt().authorities(USER)))
                .andExpect(status().isForbidden());
    }
}
package de.co2management.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.co2management.backend.TestcontainersConfiguration;
import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import de.co2management.backend.user.AppUser;
import de.co2management.backend.user.AppUserRepository;
import de.co2management.backend.user.Role;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AuthIntegrationTest {

    private static final String PASSWORD = "test-passwort";

    @Autowired private MockMvc mockMvc;
    @Autowired private AppUserRepository userRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        siteRepository.deleteAll();
        Site north = siteRepository.save(new Site("NORD", "Werk Nord"));
        userRepository.save(new AppUser("Ben Beispiel", "fuehrungskraft",
                passwordEncoder.encode(PASSWORD), Role.MANAGER, north));
    }

    // T-01-03
    @Test
    void loginWithValidCredentialsReturnsTokenWithRoleAndSite() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("fuehrungskraft", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("MANAGER"))
                .andExpect(jsonPath("$.user.siteName").value("Werk Nord"));
    }

    // T-01-04
    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("fuehrungskraft", "falsch")))
                .andExpect(status().isUnauthorized());
    }

    // T-01-05
    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    // T-01-06
    @Test
    void requestWithManipulatedTokenIsRejected() throws Exception {
        String[] parts = login().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String forgedPayload = payload.replace("MANAGER", "ADMIN");
        parts[1] = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(forgedPayload.getBytes(StandardCharsets.UTF_8));
        String forgedToken = String.join(".", parts);

        mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isUnauthorized());
    }

    // T-01-07
    @Test
    void storedPasswordIsHashed() {
        AppUser user = userRepository.findByUsername("fuehrungskraft").orElseThrow();

        assertThat(user.getPasswordHash()).isNotEqualTo(PASSWORD);
        assertThat(user.getPasswordHash()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).isTrue();
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("fuehrungskraft", PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String loginJson(String username, String password) {
        return """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);
    }
}
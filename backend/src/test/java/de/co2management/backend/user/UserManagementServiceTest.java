package de.co2management.backend.user;

import de.co2management.backend.site.SiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementService service;

    @Test
    @DisplayName("T-04-02: Führungskraft ohne Standort wird abgewiesen")
    void managerWithoutSiteIsRejected() {
        CreateUserRequest request = new CreateUserRequest(
                "Ben Beispiel", "bbeispiel", "geheim123", Role.MANAGER, null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("T-04-03: Nachhaltigkeitsbeauftragte mit Standort wird abgewiesen")
    void officerWithSiteIsRejected() {
        CreateUserRequest request = new CreateUserRequest(
                "Anna Muster", "amuster", "geheim123", Role.SUSTAINABILITY_OFFICER, 1L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("T-04-06: Eigenes Konto kann nicht gelöscht werden")
    void ownAccountCannotBeDeleted() {
        assertThatThrownBy(() -> service.delete("admin", "admin"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eigene Rolle kann nicht geändert werden")
    void ownRoleCannotBeChanged() {
        AppUser admin = new AppUser("Max Mustermann", "admin", "{noop}x", Role.ADMIN, null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.update("admin", new UpdateUserRequest(Role.USER, 1L), "admin"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }
}
package de.co2management.backend.user;

import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementService {

    private final AppUserRepository userRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository userRepository,
                                 SiteRepository siteRepository,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserInfo> findAll() {
        return userRepository.findAll(Sort.by("name")).stream()
                .map(UserInfo::from)
                .toList();
    }

    @Transactional
    public UserInfo create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Anmeldename bereits vergeben");
        }
        Site site = resolveSite(request.role(), request.siteId());
        AppUser user = new AppUser(request.name().trim(), username,
                passwordEncoder.encode(request.password()), request.role(), site);
        return UserInfo.from(userRepository.save(user));
    }

    @Transactional
    public UserInfo update(String username, UpdateUserRequest request, String currentUsername) {
        AppUser user = findUser(username);
        if (username.equals(currentUsername) && request.role() != user.getRole()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die eigene Rolle kann nicht geändert werden");
        }
        Site site = resolveSite(request.role(), request.siteId());
        user.changeRoleAndSite(request.role(), site);
        return UserInfo.from(user);
    }

    @Transactional
    public void delete(String username, String currentUsername) {
        if (username.equals(currentUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Das eigene Konto kann nicht gelöscht werden");
        }
        userRepository.delete(findUser(username));
    }

    private Site resolveSite(Role role, Long siteId) {
        if (!role.requiresSite()) {
            if (siteId != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Für diese Rolle ist kein Standort vorgesehen");
            }
            return null;
        }
        if (siteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Für diese Rolle ist ein Standort Pflicht");
        }
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Standort nicht gefunden"));
    }

    private AppUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benutzer nicht gefunden"));
    }
}
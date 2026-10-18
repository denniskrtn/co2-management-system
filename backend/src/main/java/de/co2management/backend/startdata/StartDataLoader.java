package de.co2management.backend.startdata;

import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import de.co2management.backend.user.AppUser;
import de.co2management.backend.user.AppUserRepository;
import de.co2management.backend.user.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Order(2)
@Component
@Profile("!test")
public class StartDataLoader implements CommandLineRunner {

    private final SiteRepository siteRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public StartDataLoader(SiteRepository siteRepository,
                           AppUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.demo-password}") String demoPassword) {
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        if (siteRepository.count() > 0) {
            return;
        }

        Site north = siteRepository.save(new Site("NORD", "Werk Nord"));
        Site south = siteRepository.save(new Site("SUED", "Werk Süd"));
        siteRepository.save(new Site("WEST", "Werk West"));

        userRepository.save(new AppUser("Max Mustermann", "admin",
                passwordEncoder.encode(demoPassword), Role.ADMIN, null));
        userRepository.save(new AppUser("Anna Muster", "nachhaltigkeit",
                passwordEncoder.encode(demoPassword), Role.SUSTAINABILITY_OFFICER, null));
        userRepository.save(new AppUser("Ben Beispiel", "fuehrungskraft",
                passwordEncoder.encode(demoPassword), Role.MANAGER, north));
        userRepository.save(new AppUser("Clara Test", "benutzer",
                passwordEncoder.encode(demoPassword), Role.USER, south));
    }
}
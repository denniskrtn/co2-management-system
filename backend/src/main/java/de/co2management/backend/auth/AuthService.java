package de.co2management.backend.auth;

import de.co2management.backend.user.AppUser;
import de.co2management.backend.user.AppUserRepository;
import de.co2management.backend.user.UserInfo;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final String dummyHash;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.dummyHash = passwordEncoder.encode("timing-protection-placeholder");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Optional<AppUser> candidate = userRepository.findByUsername(request.username());
        String hash = candidate.map(AppUser::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        AppUser user = candidate
                .filter(u -> passwordMatches)
                .orElseThrow(() -> new BadCredentialsException("Anmeldename oder Passwort ist falsch"));

        String token = tokenService.issueToken(user);
        return new LoginResponse(token, tokenService.getValidity().toSeconds(), UserInfo.from(user));
    }
}
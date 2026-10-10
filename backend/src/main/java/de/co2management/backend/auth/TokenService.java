package de.co2management.backend.auth;

import de.co2management.backend.user.AppUser;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration validity;

    public TokenService(JwtEncoder jwtEncoder, Clock clock,
                        @Value("${app.jwt.validity-minutes}") long validityMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.validity = Duration.ofMinutes(validityMinutes);
    }

    public String issueToken(AppUser user) {
        Instant now = clock.instant();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("co2-management-system")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(validity))
                .claim("role", user.getRole().name());

        if (user.getSite() != null) {
            claims.claim("siteId", user.getSite().getId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build()))
                .getTokenValue();
    }

    public Duration getValidity() {
        return validity;
    }
}
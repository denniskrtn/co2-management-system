package de.co2management.backend.auth;

import de.co2management.backend.user.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public record AuthenticatedUser(String username, Role role, Long siteId) {

    public static AuthenticatedUser from(Jwt jwt) {
        Number siteId = jwt.getClaim("siteId");
        return new AuthenticatedUser(
                jwt.getSubject(),
                Role.valueOf(jwt.getClaimAsString("role")),
                siteId == null ? null : siteId.longValue());
    }
}
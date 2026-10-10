package de.co2management.backend.auth;

import de.co2management.backend.user.UserInfo;

public record LoginResponse(String token, long expiresInSeconds, UserInfo user) {
}
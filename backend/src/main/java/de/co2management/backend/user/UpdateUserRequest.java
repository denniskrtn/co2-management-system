package de.co2management.backend.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(@NotNull Role role, Long siteId) {
}
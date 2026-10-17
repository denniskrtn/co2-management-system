package de.co2management.backend.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameSiteRequest(@NotBlank @Size(max = 100) String name) {
}
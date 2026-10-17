package de.co2management.backend.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 100) String name) {
}
package de.co2management.backend.emission;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEntryRequest(
        @NotNull Long siteId,
        @NotNull Long emissionSourceId,
        @NotNull @Positive @Digits(integer = 12, fraction = 3) BigDecimal quantity,
        @NotNull LocalDate entryDate) {
}
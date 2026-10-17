package de.co2management.backend.emission;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EntryInfo(Long id, String siteName, String emissionSourceName, String unit,
                        BigDecimal quantity, LocalDate entryDate,
                        BigDecimal factorValue, BigDecimal co2eKg) {

    public static EntryInfo from(EmissionEntry entry) {
        EmissionSource source = entry.getEmissionFactor().getEmissionSource();
        return new EntryInfo(entry.getId(), entry.getSite().getName(), source.getName(), source.getUnit(),
                entry.getQuantity(), entry.getEntryDate(), entry.getFactorValue(), entry.getCo2eKg());
    }
}
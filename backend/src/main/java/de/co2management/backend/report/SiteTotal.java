package de.co2management.backend.report;

import java.math.BigDecimal;

public record SiteTotal(Long siteId, String siteName, BigDecimal totalKg, BigDecimal roundedKg) {

    public SiteTotal(Long siteId, String siteName, BigDecimal totalKg) {
        this(siteId, siteName, zeroIfNull(totalKg), DisplayRounding.round(zeroIfNull(totalKg)));
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
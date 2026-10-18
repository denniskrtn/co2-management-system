package de.co2management.backend.report;

import java.math.BigDecimal;

public record SiteTotal(Long siteId, String siteName, BigDecimal totalKg) {

    public SiteTotal {
        totalKg = totalKg == null ? BigDecimal.ZERO : totalKg;
    }
}
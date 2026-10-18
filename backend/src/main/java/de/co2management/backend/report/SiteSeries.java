package de.co2management.backend.report;

import java.math.BigDecimal;
import java.util.List;

public record SiteSeries(Long siteId, String siteName, List<BigDecimal> totalsKg, List<BigDecimal> roundedKg) {
}
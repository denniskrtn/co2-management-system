package de.co2management.backend.report;

import java.math.BigDecimal;

public record MonthlySum(Long siteId, Integer year, Integer month, BigDecimal totalKg) {
}
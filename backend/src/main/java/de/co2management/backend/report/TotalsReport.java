package de.co2management.backend.report;

import java.time.LocalDate;
import java.util.List;

public record TotalsReport(LocalDate from, LocalDate to, List<SiteTotal> sites) {
}
package de.co2management.backend.report;

import java.time.LocalDate;
import java.util.List;

public record MonthlyReport(LocalDate from, LocalDate to, List<String> months, List<SiteSeries> series) {
}
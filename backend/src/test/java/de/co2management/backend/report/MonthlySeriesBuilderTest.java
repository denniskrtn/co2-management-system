package de.co2management.backend.report;

import de.co2management.backend.site.SiteInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlySeriesBuilderTest {

    @Test
    @DisplayName("T-10-01: Januar bis Juni mit Einträgen im Januar und März ergibt sechs Werte, übrige Monate 0")
    void fillsMonthsWithoutEntriesWithZero() {
        List<YearMonth> months = MonthlySeriesBuilder.monthsBetween(
                new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)));

        List<SiteSeries> series = MonthlySeriesBuilder.build(months,
                List.of(new SiteInfo(1L, "NORD", "Werk Nord")),
                List.of(new MonthlySum(1L, 2026, 1, new BigDecimal("380")),
                        new MonthlySum(1L, 2026, 3, new BigDecimal("380"))));

        assertThat(months).hasSize(6);
        assertThat(series).hasSize(1);
        assertThat(series.getFirst().totalsKg())
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("380"), BigDecimal.ZERO, new BigDecimal("380"),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
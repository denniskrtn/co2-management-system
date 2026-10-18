package de.co2management.backend.report;

import de.co2management.backend.site.SiteInfo;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MonthlySeriesBuilder {

    private MonthlySeriesBuilder() {
    }

    static List<YearMonth> monthsBetween(DateRange range) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth last = YearMonth.from(range.to());
        for (YearMonth month = YearMonth.from(range.from()); !month.isAfter(last); month = month.plusMonths(1)) {
            months.add(month);
        }
        return months;
    }

    static List<SiteSeries> build(List<YearMonth> months, List<SiteInfo> sites, List<MonthlySum> sums) {
        Map<Long, Map<YearMonth, BigDecimal>> bySite = new HashMap<>();
        for (MonthlySum sum : sums) {
            bySite.computeIfAbsent(sum.siteId(), id -> new HashMap<>())
                    .put(YearMonth.of(sum.year(), sum.month()), sum.totalKg());
        }
        return sites.stream()
                .map(site -> {
                    Map<YearMonth, BigDecimal> values = bySite.getOrDefault(site.id(), Map.of());
                    List<BigDecimal> totals = months.stream()
                            .map(month -> values.getOrDefault(month, BigDecimal.ZERO))
                            .toList();
                    List<BigDecimal> rounded = totals.stream().map(DisplayRounding::round).toList();
                    return new SiteSeries(site.id(), site.name(), totals, rounded);
                })
                .toList();
    }
}
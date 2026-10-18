package de.co2management.backend.report;

import de.co2management.backend.auth.AuthenticatedUser;
import de.co2management.backend.site.SiteInfo;
import de.co2management.backend.site.SiteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('SUSTAINABILITY_OFFICER', 'MANAGER', 'USER')")
public class ReportService {

    private final ReportRepository reportRepository;
    private final SiteRepository siteRepository;
    private final Clock clock;

    public ReportService(ReportRepository reportRepository, SiteRepository siteRepository, Clock clock) {
        this.reportRepository = reportRepository;
        this.siteRepository = siteRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public TotalsReport totals(AuthenticatedUser user, LocalDate from, LocalDate to, Long requestedSiteId) {
        DateRange range = resolveRange(from, to);
        Long siteId = resolveSiteScope(user, requestedSiteId);
        List<SiteTotal> totals = siteId == null
                ? reportRepository.totalsForAllSites(range.from(), range.to())
                : reportRepository.totalsForSite(siteId, range.from(), range.to());
        return new TotalsReport(range.from(), range.to(), totals);
    }

    @PreAuthorize("hasAnyRole('SUSTAINABILITY_OFFICER', 'MANAGER')")
    @Transactional(readOnly = true)
    public MonthlyReport monthly(AuthenticatedUser user, LocalDate from, LocalDate to, Long requestedSiteId) {
        DateRange range = resolveRange(from, to);
        Long siteId = resolveSiteScope(user, requestedSiteId);
        List<SiteInfo> sites = siteId == null
                ? siteRepository.findAll(Sort.by("name")).stream().map(SiteInfo::from).toList()
                : siteRepository.findById(siteId).map(SiteInfo::from).stream().toList();
        List<MonthlySum> sums = siteId == null
                ? reportRepository.monthlySumsForAllSites(range.from(), range.to())
                : reportRepository.monthlySumsForSite(siteId, range.from(), range.to());
        List<YearMonth> months = MonthlySeriesBuilder.monthsBetween(range);
        return new MonthlyReport(range.from(), range.to(),
                months.stream().map(YearMonth::toString).toList(),
                MonthlySeriesBuilder.build(months, sites, sums));
    }

    DateRange resolveRange(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            int year = LocalDate.now(clock).getYear();
            return new DateRange(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start- und Enddatum sind anzugeben");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Das Startdatum liegt nach dem Enddatum");
        }
        return new DateRange(from, to);
    }

    Long resolveSiteScope(AuthenticatedUser user, Long requestedSiteId) {
        if (!user.role().requiresSite()) {
            return requestedSiteId;
        }
        if (requestedSiteId != null && !requestedSiteId.equals(user.siteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Zugriff auf diesen Standort");
        }
        return user.siteId();
    }
}
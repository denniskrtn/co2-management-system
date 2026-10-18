package de.co2management.backend.report;

import de.co2management.backend.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('SUSTAINABILITY_OFFICER', 'MANAGER', 'USER')")
public class ReportService {

    private final ReportRepository reportRepository;
    private final Clock clock;

    public ReportService(ReportRepository reportRepository, Clock clock) {
        this.reportRepository = reportRepository;
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
package de.co2management.backend.report;

import de.co2management.backend.auth.AuthenticatedUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportPdfService reportPdfService;

    public ReportController(ReportService reportService, ReportPdfService reportPdfService) {
        this.reportService = reportService;
        this.reportPdfService = reportPdfService;
    }

    @GetMapping("/totals")
    public TotalsReport totals(@AuthenticationPrincipal Jwt jwt,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               @RequestParam(required = false) Long siteId) {
        return reportService.totals(AuthenticatedUser.from(jwt), from, to, siteId);
    }

    @GetMapping("/monthly")
    public MonthlyReport monthly(@AuthenticationPrincipal Jwt jwt,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                 @RequestParam(required = false) Long siteId) {
        return reportService.monthly(AuthenticatedUser.from(jwt), from, to, siteId);
    }

    @GetMapping("/totals/pdf")
    public ResponseEntity<byte[]> totalsPdf(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] pdf = reportPdfService.totalsPdf(AuthenticatedUser.from(jwt), from, to);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("emissionsbericht.pdf").build().toString())
                .body(pdf);
    }
}
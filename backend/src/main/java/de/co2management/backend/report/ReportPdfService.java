package de.co2management.backend.report;

import de.co2management.backend.auth.AuthenticatedUser;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@PreAuthorize("hasAnyRole('SUSTAINABILITY_OFFICER', 'MANAGER', 'USER')")
public class ReportPdfService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 9);

    private final ReportService reportService;
    private final Clock clock;

    public ReportPdfService(ReportService reportService, Clock clock) {
        this.reportService = reportService;
        this.clock = clock;
    }

    public byte[] totalsPdf(AuthenticatedUser user, LocalDate from, LocalDate to) {
        TotalsReport report = reportService.totals(user, from, to, null);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Emissionsbericht", TITLE);
            title.setSpacingAfter(12f);
            document.add(title);
            document.add(new Paragraph("Zeitraum: " + report.from().format(DATE)
                    + " bis " + report.to().format(DATE), NORMAL));
            Paragraph created = new Paragraph("Erstellt am: " + LocalDateTime.now(clock).format(DATE_TIME), NORMAL);
            created.setSpacingAfter(12f);
            document.add(created);

            document.add(totalsTable(report));

            Paragraph note = new Paragraph("Gesamtemissionen in kg CO2e, kaufmännisch auf zwei Nachkommastellen gerundet.", SMALL);
            note.setSpacingBefore(8f);
            document.add(note);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Der PDF-Bericht konnte nicht erzeugt werden", e);
        }
    }

    private static PdfPTable totalsTable(TotalsReport report) {
        PdfPTable table = new PdfPTable(new float[]{3f, 2f});
        table.setWidthPercentage(100f);
        table.setHeaderRows(1);
        table.addCell(headerCell("Standort", Element.ALIGN_LEFT));
        table.addCell(headerCell("Gesamtemissionen (kg CO2e)", Element.ALIGN_RIGHT));
        for (SiteTotal site : report.sites()) {
            table.addCell(cell(site.siteName(), NORMAL, Element.ALIGN_LEFT));
            table.addCell(cell(formatKg(site.roundedKg()), NORMAL, Element.ALIGN_RIGHT));
        }
        return table;
    }

    private static PdfPCell headerCell(String text, int alignment) {
        PdfPCell cell = cell(text, BOLD, alignment);
        cell.setGrayFill(0.9f);
        return cell;
    }

    private static PdfPCell cell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6f);
        return cell;
    }

    private static String formatKg(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value);
    }
}
package de.co2management.backend.emission;

import de.co2management.backend.site.Site;
import de.co2management.backend.site.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmissionEntryServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    @Mock
    private EmissionEntryRepository entryRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private EmissionSourceRepository sourceRepository;

    @Mock
    private EmissionFactorRepository factorRepository;

    private final EmissionSource strom = new EmissionSource("Strom", "kWh", Scope.SCOPE_2);

    private EmissionEntryService service;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        Clock clock = Clock.fixed(TODAY.atStartOfDay(zone).toInstant(), zone);
        service = new EmissionEntryService(entryRepository, siteRepository, sourceRepository,
                factorRepository, new EmissionCalculator(), clock);
    }

    private void givenSiteAndSource() {
        when(siteRepository.findById(1L)).thenReturn(Optional.of(new Site("NORD", "Werk Nord")));
        when(sourceRepository.findById(1L)).thenReturn(Optional.of(strom));
    }

    private void givenFactor(int year, String value) {
        when(factorRepository.findByEmissionSourceIdAndYear(1L, year))
                .thenReturn(Optional.of(new EmissionFactor(strom, year, new BigDecimal(value))));
        when(entryRepository.save(any(EmissionEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CreateEntryRequest request(LocalDate date) {
        return new CreateEntryRequest(1L, 1L, new BigDecimal("1000"), date);
    }

    @Test
    @DisplayName("T-05-03: Datum 31.12.2025 verwendet den Faktor von 2025")
    void lastDayOf2025UsesFactorOf2025() {
        givenSiteAndSource();
        givenFactor(2025, "0.400");

        EntryInfo result = service.create(request(LocalDate.of(2025, 12, 31)));

        assertThat(result.factorValue()).isEqualByComparingTo("0.400");
        assertThat(result.co2eKg()).isEqualByComparingTo("400");
    }

    @Test
    @DisplayName("T-05-04: Datum 01.01.2026 verwendet den Faktor von 2026")
    void firstDayOf2026UsesFactorOf2026() {
        givenSiteAndSource();
        givenFactor(2026, "0.380");

        EntryInfo result = service.create(request(LocalDate.of(2026, 1, 1)));

        assertThat(result.factorValue()).isEqualByComparingTo("0.380");
        assertThat(result.co2eKg()).isEqualByComparingTo("380");
    }

    @Test
    @DisplayName("T-05-09: Datum heute wird angenommen")
    void todayIsAccepted() {
        givenSiteAndSource();
        givenFactor(2026, "0.380");

        EntryInfo result = service.create(request(TODAY));

        assertThat(result.entryDate()).isEqualTo(TODAY);
        verify(entryRepository).save(any(EmissionEntry.class));
    }

    @Test
    @DisplayName("T-05-10: Datum morgen wird abgewiesen")
    void tomorrowIsRejected() {
        assertThatThrownBy(() -> service.create(request(TODAY.plusDays(1))))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(entryRepository, never()).save(any());
    }

    @Test
    @DisplayName("T-05-11: Jahr ohne Faktor wird mit Meldung abgewiesen")
    void yearWithoutFactorIsRejected() {
        givenSiteAndSource();

        assertThatThrownBy(() -> service.create(request(LocalDate.of(2024, 5, 1))))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).contains("2024");
                });
        verify(entryRepository, never()).save(any());
    }
}
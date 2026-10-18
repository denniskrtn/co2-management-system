package de.co2management.backend.report;

import de.co2management.backend.emission.EmissionEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends Repository<EmissionEntry, Long> {

    @Query("""
            select new de.co2management.backend.report.SiteTotal(s.id, s.name, sum(e.co2eKg))
            from Site s
            left join EmissionEntry e on e.site = s and e.entryDate between :from and :to
            group by s.id, s.name
            order by s.name
            """)
    List<SiteTotal> totalsForAllSites(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new de.co2management.backend.report.SiteTotal(s.id, s.name, sum(e.co2eKg))
            from Site s
            left join EmissionEntry e on e.site = s and e.entryDate between :from and :to
            where s.id = :siteId
            group by s.id, s.name
            """)
    List<SiteTotal> totalsForSite(@Param("siteId") Long siteId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    @Query("""
            select new de.co2management.backend.report.MonthlySum(
                e.site.id, year(e.entryDate), month(e.entryDate), sum(e.co2eKg))
            from EmissionEntry e
            where e.entryDate between :from and :to
            group by e.site.id, year(e.entryDate), month(e.entryDate)
            """)
    List<MonthlySum> monthlySumsForAllSites(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new de.co2management.backend.report.MonthlySum(
                e.site.id, year(e.entryDate), month(e.entryDate), sum(e.co2eKg))
            from EmissionEntry e
            where e.site.id = :siteId and e.entryDate between :from and :to
            group by e.site.id, year(e.entryDate), month(e.entryDate)
            """)
    List<MonthlySum> monthlySumsForSite(@Param("siteId") Long siteId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

}
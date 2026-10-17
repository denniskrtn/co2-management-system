package de.co2management.backend.emission;

import de.co2management.backend.site.Site;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emission_entry")
public class EmissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emission_factor_id", nullable = false)
    private EmissionFactor emissionFactor;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal factorValue;

    @Column(name = "co2e_kg", nullable = false, precision = 27, scale = 9)
    private BigDecimal co2eKg;

    protected EmissionEntry() {
    }

    public EmissionEntry(Site site, EmissionFactor emissionFactor, BigDecimal quantity,
                         LocalDate entryDate, BigDecimal co2eKg) {
        this.site = site;
        this.emissionFactor = emissionFactor;
        this.quantity = quantity;
        this.entryDate = entryDate;
        this.factorValue = emissionFactor.getFactorValue();
        this.co2eKg = co2eKg;
    }

    public Long getId() {
        return id;
    }

    public Site getSite() {
        return site;
    }

    public EmissionFactor getEmissionFactor() {
        return emissionFactor;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public BigDecimal getFactorValue() {
        return factorValue;
    }

    public BigDecimal getCo2eKg() {
        return co2eKg;
    }
}
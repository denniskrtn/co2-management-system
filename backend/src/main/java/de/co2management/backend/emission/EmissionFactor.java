package de.co2management.backend.emission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "emission_factor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"emission_source_id", "year"}))
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "emission_source_id", nullable = false)
    private EmissionSource emissionSource;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal factorValue;

    protected EmissionFactor() {
    }

    public EmissionFactor(EmissionSource emissionSource, int year, BigDecimal factorValue) {
        this.emissionSource = emissionSource;
        this.year = year;
        this.factorValue = factorValue;
    }

    public Long getId() {
        return id;
    }

    public EmissionSource getEmissionSource() {
        return emissionSource;
    }

    public int getYear() {
        return year;
    }

    public BigDecimal getFactorValue() {
        return factorValue;
    }
}